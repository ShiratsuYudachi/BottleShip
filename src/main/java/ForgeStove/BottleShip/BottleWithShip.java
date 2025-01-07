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
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.*;

import java.util.List;

import static ForgeStove.BottleShip.BottleShip.*;
import static net.minecraft.ChatFormatting.GRAY;
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
		tooltip.add(translatable("tooltip." + MODID + ".id", nullToEmpty(nbt.getString("ID"))).withStyle(GRAY));
		tooltip.add(translatable("tooltip." + MODID + ".name", nullToEmpty(nbt.getString("Name"))).withStyle(GRAY));
		tooltip.add(translatable("tooltip." + MODID + ".size", nullToEmpty(nbt.getString("Size"))).withStyle(GRAY));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level,
			@NotNull Player player,
			@NotNull InteractionHand hand
	) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (level.isClientSide()) return pass(itemStack);
		if (itemStack.getTag() == null) return fail(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
		long id = Long.parseLong(itemStack.getTag().getString("ID"));
		if (!SHIPS.containsKey(id)) return fail(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
		Ship ship = SHIPS.get(id).ship();
		AABBdc shipAABB = ship.getWorldAABB();
		double height = shipAABB.maxY() - shipAABB.minY();
		double depth = shipAABB.maxZ() - shipAABB.minZ();
		Vec3 playerPosition = player.position();
		double distance = 5;
		float yaw = player.getYRot();
		float pitch = player.getXRot();
		double dx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		double dy = -Math.sin(Math.toRadians(pitch));
		double dz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		double targetX = playerPosition.x + dx * distance;
		double targetY = playerPosition.y + dy * distance;
		double targetZ = playerPosition.z + dz * distance;
		targetX += (dx * (depth / 2));
		targetY += (dy * (height / 2));
		targetZ += (dz * (depth / 2));
		MinecraftServer server = level.getServer();
		Commands.vsTeleport(ship, server, targetX, targetY + height, targetZ);
		if (((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship, server, false);
		SHIPS.remove(id);
		return success(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
	}
}