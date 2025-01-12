package ForgeStove.BottleShip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.*;

import java.util.*;

import static ForgeStove.BottleShip.BottleShip.*;
import static java.lang.Math.*;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.network.chat.Component.*;
import static net.minecraft.world.InteractionResultHolder.*;
public class BottleWithShip extends Item {
	public BottleWithShip(Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(
			@NotNull ItemStack itemStack,
			Level level,
			@NotNull List<Component> tooltip,
			@NotNull TooltipFlag flag
	) {
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(translatable("tooltip." + MODID + ".id", nullToEmpty(nbt.getString("ID"))).withStyle(AQUA));
		tooltip.add(translatable("tooltip." + MODID + ".name", nullToEmpty(nbt.getString("Name"))).withStyle(AQUA));
		tooltip.add(translatable("tooltip." + MODID + ".size", nullToEmpty(nbt.getString("Size"))).withStyle(AQUA));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level,
			@NotNull Player player,
			@NotNull InteractionHand hand
	) {
		ItemStack currentStack = player.getItemInHand(hand);
		if (level.isClientSide()) return fail(currentStack);
		ItemStack newStack = new ItemStack(BOTTLE_WITHOUT_SHIP.get());
		if (currentStack.getTag() == null) return fail(newStack);
		long id = Long.parseLong(currentStack.getTag().getString("ID"));
		if (!SHIPS.containsKey(id)) return fail(newStack);
		if (SHIPS.get(id).level() != level) return fail(currentStack);
		Vec3 playerPosition = player.position();
		Ship ship = SHIPS.get(id).ship();
		AABBdc worldAABB = ship.getWorldAABB();
		double depth = worldAABB.maxZ() - worldAABB.minZ();
		double yawRadians = toRadians(player.getYRot());
		double pitchRadians = toRadians(player.getXRot());
		double dx = -sin(yawRadians) * cos(pitchRadians);
		double dy = -sin(pitchRadians);
		double dz = cos(yawRadians) * cos(pitchRadians);
		double targetX = playerPosition.x + dx * 5;
		double targetY = playerPosition.y + dy * 5;
		double targetZ = playerPosition.z + dz * 5;
		Vector3dc massCenter = ((ServerShip) ship).getInertiaData().getCenterOfMassInShip();
		double massHeight = massCenter.y() - Objects.requireNonNull(ship.getShipAABB()).minY();
		targetX += (dx * (depth / 2));
		targetY += (dy * massHeight);
		targetZ += (dz * (depth / 2));
		MinecraftServer server = level.getServer();
		Commands.vmodTeleport(id, server, (int) targetX, (int) (targetY + massHeight), (int) targetZ);
		if (((ServerShip) ship).isStatic()) Commands.vsSetStatic(id, server, false);
		SHIPS.remove(id);
		return success(newStack);
	}
}
