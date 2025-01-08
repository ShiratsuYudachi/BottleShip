package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
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
		if (level.isClientSide()) return PASS;
		BlockPos blockPos = context.getClickedPos();
		Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
		Player player = context.getPlayer();
		if (ship == null || player == null) return FAIL;
		SHIPS.put(ship.getId(), new ShipData(ship, level));
		AABBdc worldAABB = ship.getWorldAABB();
		MinecraftServer server = player.getServer();
		Commands.vsTeleport(
				ship.getId(),
				server, blockPos.getX(), -blockPos.getY(),
				blockPos.getZ()
		);
		player.sendSystemMessage(Component.literal(blockPos.toShortString()));
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship.getId(), server, true);
		ItemStack newStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		nbt.putString("Name", Objects.requireNonNull(ship.getSlug()));
		nbt.putString(
				"Size", "( x: %d y: %d z: %d )".formatted(
						(int) (worldAABB.maxX() - worldAABB.minX()),
						(int) (worldAABB.maxY() - worldAABB.minY()),
						(int) (worldAABB.maxZ() - worldAABB.minZ())
				)
		);
		newStack.setTag(nbt);
		player.setItemInHand(context.getHand(), newStack);
		return SUCCESS;
	}
}
