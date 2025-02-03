package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;

import static ForgeStove.BottleShip.BottleShip.*;
import static ForgeStove.BottleShip.Commands.*;
import static ForgeStove.BottleShip.Config.*;
import static net.minecraft.sounds.SoundEvents.BOTTLE_FILL;
import static net.minecraft.sounds.SoundSource.PLAYERS;
import static net.minecraft.world.InteractionResult.*;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;
public class BottleWithoutShipItem extends Item {
	private UseOnContext context;
	public BottleWithoutShipItem(Properties properties) {
		super(properties);
	}
	@Override public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (level.isClientSide()) return FAIL;
		Player player = useOnContext.getPlayer();
		if (player == null || player instanceof FakePlayer) return FAIL;
		if (player.getVehicle() != null) player.stopRiding();
		context = useOnContext;
		player.startUsingItem(useOnContext.getHand());
		return CONSUME;
	}
	@Override
	public void onUseTick(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			@NotNull ItemStack itemStack,
			int tickLeft
	) {
		if (!level.isClientSide()) return;
		onUseTickCommon(level, livingEntity, getUseDuration(itemStack) - tickLeft, bottleWithoutShipChargeTime.get());
	}
	@Override public int getUseDuration(@NotNull ItemStack itemStack) {
		return 100000;
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level, @NotNull LivingEntity livingEntity, int tickLeft
	) {
		if (level.isClientSide()) return;
		if ((getUseDuration(itemStack) - tickLeft) * 1000 / 20 < bottleWithoutShipChargeTime.get()) return;
		MinecraftServer server = level.getServer();
		BlockPos blockPos = context.getClickedPos();
		ServerShip ship = getShipManagingPos((ServerLevel) level, blockPos);
		if (!(livingEntity instanceof Player player)) return;
		if (ship == null) return;
		long id = ship.getId();
		setStatic(ship, server, true);
		teleport(
				player.getName().getString(),
				ship,
				server,
				(long) (-blockPos.getX() - player.getX()),
				(long) (blockPos.getY() - player.getY()),
				(long) (-blockPos.getZ() - player.getZ())
		);
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(id));
		if (ship.getSlug() != null) nbt.putString("Name", ship.getSlug());
		AABBic shipAABB = ship.getShipAABB();
		if (shipAABB == null) return;
		nbt.putString(
				"Size", String.format(
						"[§bX:§a%d §bY:§a%d §bZ:§a%d§f]",
						shipAABB.maxX() - shipAABB.minX(),
						shipAABB.maxY() - shipAABB.minY(),
						shipAABB.maxZ() - shipAABB.minZ()
				)
		);
		ItemStack newStack = new ItemStack(BOTTLE_WITH_SHIP.get());
		newStack.setTag(nbt);
		player.setItemInHand(player.getUsedItemHand(), newStack);
		player.getCooldowns().addCooldown(newStack.getItem(), bottleWithoutShipCooldown.get());
		level.playSound(null, player.getX(), player.getY(), player.getZ(), BOTTLE_FILL, PLAYERS, 1.0F, 1.0F);
	}
	@Override public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return UseAnim.BOW;
	}
}
