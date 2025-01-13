// Copyright (C) 2025 ForgeStove
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

import static net.minecraft.world.InteractionResult.*;
public class BottleWithoutShip extends Item {
	public BottleWithoutShip(Properties properties) {
		super(properties);
	}
	@Override public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return FAIL;
		Player player = context.getPlayer();
		if (player == null || player instanceof FakePlayer || player.getVehicle() != null) return FAIL;
		BlockPos blockPos = context.getClickedPos();
		Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
		if (ship == null) return FAIL;
		long id = ship.getId();
		MinecraftServer server = level.getServer();
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(id, server, true);
		Commands.vmodTeleport(
				player.getName().toString(),
				id,
				server,
				(int) (-blockPos.getX() - player.getX()),
				(int) (-blockPos.getY() - player.getY()),
				(int) (-blockPos.getZ() - player.getZ())
		);
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
