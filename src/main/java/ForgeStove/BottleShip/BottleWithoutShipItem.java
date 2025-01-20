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
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import static net.minecraft.world.InteractionResult.*;
public class BottleWithoutShipItem extends Item {
	private UseOnContext context;
	private long time;
	public BottleWithoutShipItem(Properties properties) {
		super(properties);
	}
	@Override public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (level.isClientSide()) return FAIL;
		Player player = useOnContext.getPlayer();
		if (player == null || player instanceof FakePlayer || player.getVehicle() != null) return FAIL;
		context = useOnContext;
		player.startUsingItem(useOnContext.getHand());
		time = System.currentTimeMillis();
		return CONSUME;
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull LivingEntity livingEntity, int timeLeft
	) {
		if (level.isClientSide()) return;
		if (System.currentTimeMillis() - time < Config.bottleWithoutShipChargeTime.get()) return;
		MinecraftServer server = level.getServer();
		BlockPos blockPos = context.getClickedPos();
		Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
		Player player = (Player) livingEntity;
		if (ship == null) return;
		long id = ship.getId();
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(id, server, true);
		Commands.vmodTeleport(
				player.getName().toString(),
				id,
				server, (int) (-blockPos.getX() - player.getX()), (int) (blockPos.getY() - player.getY()),
				(int) (-blockPos.getZ() - player.getZ())
		);
		ItemStack newStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(id));
		nbt.putString("Name", String.valueOf(Component.nullToEmpty(ship.getSlug())));
		AABBic shipAABB = ship.getShipAABB();
		if (shipAABB != null) nbt.putString(
				"Size", "( x: %d y: %d z: %d )".formatted(
						shipAABB.maxX() - shipAABB.minX(),
						shipAABB.maxY() - shipAABB.minY(),
						shipAABB.maxZ() - shipAABB.minZ()
				)
		);
		newStack.setTag(nbt);
		player.setItemInHand(player.getUsedItemHand(), newStack);
		player.getCooldowns().addCooldown(newStack.getItem(), Config.bottleWithoutShipCooldown.get());
		level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.BOTTLE_FILL,
				player.getSoundSource(),
				1.0F,
				1.0F
		);
	}
	@Override public int getUseDuration(@NotNull ItemStack itemStack) {
		return 100000;
	}
	@Override public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return UseAnim.BOW;
	}
}
