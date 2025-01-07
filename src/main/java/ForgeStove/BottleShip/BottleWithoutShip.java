package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.*;

import java.util.Objects;

import static ForgeStove.BottleShip.BottleShip.SHIPS;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;
public class BottleWithoutShip extends Item {
	public BottleWithoutShip(Properties properties) {
		super(properties);
	}
	@Override public @NotNull InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.CONSUME;
		BlockPos blockPos = context.getClickedPos();
		Ship ship = getShipManagingPos(level, blockPos);
		Player player = context.getPlayer();
		if (ship == null || player == null) return InteractionResult.FAIL;
		SHIPS.put(ship.getId(), new ShipData(ship, level));
		AABBdc shipAABB = ship.getWorldAABB();
		MinecraftServer server = level.getServer();
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship, server, true);
		Commands.vsTeleport(
				ship,
				server,
				blockPos.getX(), blockPos.getY() + (shipAABB.maxY() - shipAABB.minY()) * 2,
				blockPos.getZ()
		);
		ItemStack newItemStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		CompoundTag nbt = newItemStack.getOrCreateTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		nbt.putString("Name", Objects.requireNonNull(ship.getSlug()));
		nbt.putString(
				"Size", "( x: %d y: %d z: %d )".formatted(
						(int) (shipAABB.maxX() - shipAABB.minX()),
						(int) (shipAABB.maxY() - shipAABB.minY()),
						(int) (shipAABB.maxZ() - shipAABB.minZ())
				)
		);
		newItemStack.setTag(nbt);
		player.getInventory().removeItem(context.getItemInHand());
		player.addItem(newItemStack);
		return InteractionResult.CONSUME;
	}
}