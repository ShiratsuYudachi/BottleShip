package ForgeStove.BottleShip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

import static ForgeStove.BottleShip.BottleShip.*;
import static ForgeStove.BottleShip.Commands.*;
import static ForgeStove.BottleShip.Config.*;
import static java.lang.Math.*;
import static net.minecraft.network.chat.Component.*;
import static net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY;
import static net.minecraft.sounds.SoundSource.PLAYERS;
import static net.minecraft.world.InteractionResultHolder.*;
import static net.minecraft.world.item.UseAnim.BOW;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getVsPipeline;
public class BottleWithShipItem extends Item {
	public BottleWithShipItem(Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(
			@NotNull ItemStack itemStack,
			Level level,
			@NotNull List<Component> tooltip,
			@NotNull TooltipFlag flag
	) {
		if (level == null) return;
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(translatable("tooltip." + MODID + ".id", literal(String.format("§b%s§f", nbt.getString("ID")))));
		tooltip.add(translatable(
				"tooltip." + MODID + ".name",
				literal(String.format("§b%s§f", nbt.getString("Name")))
		));
		tooltip.add(translatable("tooltip." + MODID + ".size", literal(nbt.getString("Size"))));
	}
	@Override
	public void onUseTick(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			@NotNull ItemStack itemStack,
			int tickLeft
	) {
		if (!level.isClientSide()) return;
		onUseTickCommon(livingEntity, getUseDuration(itemStack) - tickLeft, bottleWithShipChargeTime.get());
	}
	@Override public int getUseDuration(@NotNull ItemStack itemStack) {
		return 100000;
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level,
			@NotNull Player player,
			@NotNull InteractionHand hand
	) {
		ItemStack currentStack = player.getItemInHand(hand);
		if (level.isClientSide()) return fail(currentStack);
		player.startUsingItem(hand);
		return consume(currentStack);
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull LivingEntity livingEntity, int tickLeft
	) {
		if (level.isClientSide()) return;
		int tickCount = getUseDuration(itemStack) - tickLeft;
		if (tickCount * 1000 / 20 < bottleWithShipChargeTime.get()) return;
		long strength = min(tickCount / 20 * bottleWithShipChargeStrength.get(), bottleWithShipChargeTime.get());
		if (!(livingEntity instanceof Player player)) return;
		if (itemStack.getTag() == null) return;
		long shipID = Long.parseLong(itemStack.getTag().getString("ID"));
		Vec3 playerPosition = player.position();
		MinecraftServer server = level.getServer();
		if (server == null) return;
		ServerShip ship = getVsPipeline(server).getShipWorld().getAllShips().getById(shipID);
		if (ship == null) return;
		AABBdc worldAABB = ship.getWorldAABB();
		double depth = worldAABB.maxY() - worldAABB.minY();
		double yawRadians = toRadians(player.getYRot());
		double pitchRadians = toRadians(player.getXRot());
		double dx = -sin(yawRadians) * cos(pitchRadians);
		double dy = -sin(pitchRadians);
		double dz = cos(yawRadians) * cos(pitchRadians);
		double targetX = playerPosition.x + dx * strength;
		double targetY = playerPosition.y + dy * strength;
		double targetZ = playerPosition.z + dz * strength;
		if (ship.getShipAABB() == null) return;
		double massHeight = ship.getInertiaData().getCenterOfMassInShip().y() - ship.getShipAABB().minY();
		targetX += (dx * (depth / 2));
		targetY += (dy * massHeight);
		targetZ += (dz * (depth / 2));
		teleport(
				player.getName().getString(),
				ship,
				server,
				(long) (targetX - player.getX()),
				(long) (targetY + massHeight - player.getY()),
				(long) (targetZ - player.getZ())
		);
		setStatic(ship, server, false);
		ItemStack newStack = new ItemStack(BOTTLE_WITHOUT_SHIP.get());
		player.setItemInHand(player.getUsedItemHand(), newStack);
		player.getCooldowns().addCooldown(newStack.getItem(), bottleWithShipCooldown.get());
		level.playSound(null, player.getX(), player.getY(), player.getZ(), BOTTLE_EMPTY, PLAYERS, 1.0F, 1.0F);
	}
	@Override public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return BOW;
	}
}
