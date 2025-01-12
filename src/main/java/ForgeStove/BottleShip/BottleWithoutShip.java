package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

import static ForgeStove.BottleShip.BottleShip.SHIPS;
import static net.minecraft.world.InteractionResult.*;
public class BottleWithoutShip extends Item {
	public BottleWithoutShip(Properties properties) {
		super(properties);
	}
	@Override public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return FAIL;
		Player player = context.getPlayer();
		if (!(player instanceof ServerPlayer) || player.getVehicle() != null) return FAIL;
		BlockPos blockPos = context.getClickedPos();
		Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
		if (ship == null) return FAIL;
		long id = ship.getId();
		SHIPS.put(id, new ShipData(ship, level));
		MinecraftServer server = level.getServer();
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(id, server, true);
		Commands.vmodTeleport(id, server, -blockPos.getX(), blockPos.getY(), -blockPos.getZ());
		ItemStack newStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(id));
		nbt.putString("Name", Objects.requireNonNull(ship.getSlug()));
		AABBic shipAABB = ship.getShipAABB();
		if (shipAABB != null) nbt.putString(
				"Size", "( x: %d y: %d z: %d )".formatted(
						shipAABB.maxX() - shipAABB.minX(),
						shipAABB.maxY() - shipAABB.minY(),
						shipAABB.maxZ() - shipAABB.minZ()
				)
		);
		newStack.setTag(nbt);
		player.setItemInHand(context.getHand(), newStack);
		return SUCCESS;
	}
}
