package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
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

import static ForgeStove.BottleShip.BottleShip.SHIPS;
import static net.minecraft.network.chat.Component.*;
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
		tooltip.add(translatable("tooltip.bottle_ship.id", nullToEmpty(nbt.getString("ID"))));
		tooltip.add(translatable("tooltip.bottle_ship.name", nullToEmpty(nbt.getString("Name"))));
		tooltip.add(translatable("tooltip.bottle_ship.size", nullToEmpty(nbt.getString("Size"))));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level,
			@NotNull Player player,
			@NotNull InteractionHand hand
	) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (level.isClientSide()) return InteractionResultHolder.pass(itemStack);
		if (itemStack.getTag() == null)
			return InteractionResultHolder.fail(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
		long id = Long.parseLong(itemStack.getTag().getString("ID"));
		if (!SHIPS.containsKey(id))
			return InteractionResultHolder.fail(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
		Ship ship = SHIPS.get(id).ship;
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
		while (true) {
			boolean hasBlock = false;
			for (BlockPos pos : BlockPos.betweenClosed(
					new BlockPos(
							(int) (targetX - (depth / 2)),
							(int) targetY,
							(int) (targetZ - (depth / 2))
					),
					new BlockPos((int) (targetX + (depth / 2)), (int) targetY, (int) (targetZ + (depth / 2)))
			))
				if (!level.getBlockState(pos).isAir()) {
					hasBlock = true;
					break;
				}
			if (!hasBlock) break;
			targetY = Math.ceil(targetY);
		}
		MinecraftServer server = level.getServer();
		Commands.vsTeleport(ship, server, targetX, targetY + 1, targetZ);
		if (((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship, server, false);
		SHIPS.remove(id);
		return InteractionResultHolder.success(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
	}
}
