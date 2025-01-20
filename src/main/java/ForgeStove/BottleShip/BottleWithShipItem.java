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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

import static ForgeStove.BottleShip.BottleShip.*;
import static java.lang.Math.*;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.network.chat.Component.*;
import static net.minecraft.world.InteractionResultHolder.*;
public class BottleWithShipItem extends Item {
	private long time;
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
		player.startUsingItem(hand);
		time = System.currentTimeMillis();
		return consume(currentStack);
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull LivingEntity livingEntity, int timeLeft
	) {
		long strength = min(
				(System.currentTimeMillis() - time) / 1000 * Config.bottleWithShipChargeStrength.get(),
				Config.bottleWithShipChargeTime.get()
		);
		Player player = (Player) livingEntity;
		ItemStack newStack = new ItemStack(BOTTLE_WITHOUT_SHIP.get());
		if (itemStack.getTag() == null) return;
		long shipID = Long.parseLong(itemStack.getTag().getString("ID"));
		Vec3 playerPosition = player.position();
		Ship ship = VSGameUtilsKt.getAllShips(level).getById(shipID);
		if (ship == null) return;
		AABBdc worldAABB = ship.getWorldAABB();
		double depth = worldAABB.maxZ() - worldAABB.minZ();
		double yawRadians = toRadians(player.getYRot());
		double pitchRadians = toRadians(player.getXRot());
		double dx = -sin(yawRadians) * cos(pitchRadians);
		double dy = -sin(pitchRadians);
		double dz = cos(yawRadians) * cos(pitchRadians);
		double targetX = playerPosition.x + dx * strength;
		double targetY = playerPosition.y + dy * strength;
		double targetZ = playerPosition.z + dz * strength;
		Vector3dc massCenter = ((ServerShip) ship).getInertiaData().getCenterOfMassInShip();
		double massHeight = massCenter.y() - Objects.requireNonNull(ship.getShipAABB()).minY();
		targetX += (dx * (depth / 2));
		targetY += (dy * massHeight);
		targetZ += (dz * (depth / 2));
		MinecraftServer server = level.getServer();
		Commands.vmodTeleport(
				player.getName().toString(),
				shipID,
				server,
				(int) (targetX - player.getX()),
				(int) (targetY + massHeight - player.getY()),
				(int) (targetZ - player.getZ())
		);
		if (((ServerShip) ship).isStatic()) Commands.vsSetStatic(shipID, server, false);
		player.setItemInHand(player.getUsedItemHand(), newStack);
		player.getCooldowns().addCooldown(newStack.getItem(), Config.bottleWithShipCooldown.get());
		level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.BOTTLE_EMPTY,
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
