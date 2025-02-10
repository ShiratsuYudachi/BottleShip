package ForgeStove.BottleShip;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.api.ServerShipUser;
import org.valkyrienskies.core.impl.api.ShipForcesInducer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.impl.datastructures.DenseBlockPosSet;

import java.util.List;

import static ForgeStove.BottleShip.BottleShip.BOTTLE_WITHOUT_SHIP;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY;
import static net.minecraft.sounds.SoundSource.PLAYERS;

public class BottleWithShipItem extends Item {

	public BottleWithShipItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (level == null) return;
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(translatable("tooltip.bottle_ship.id", Component.literal(String.format("§b%s§f", nbt.getString("ID")))));
		tooltip.add(translatable("tooltip.bottle_ship.name", Component.literal(String.format("§b%s§f", nbt.getString("Name")))));
		tooltip.add(translatable("tooltip.bottle_ship.size", Component.literal(nbt.getString("Size"))));
	}

	@Override
	public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack, int tickLeft) {
		if (level.isClientSide()) {
			if (livingEntity instanceof Player player) {
				int tickCount = getUseDuration(itemStack) - tickLeft;
				BottleShip.onUseTickCommon(level, livingEntity, tickCount, 100);
			}
		}
	}

	@Override
	public int getUseDuration(@NotNull ItemStack itemStack) {
		return 100000;
	}

	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return UseAnim.BOW;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack currentStack = player.getItemInHand(hand);
		if (level.isClientSide()) return InteractionResultHolder.pass(currentStack);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(currentStack);
	}

	@Override
	public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int tickLeft) {
		if (level.isClientSide()) return;
		int tickCount = getUseDuration(itemStack) - tickLeft;
		if (tickCount * 1000 / 20 < 100) return;

		if (!(livingEntity instanceof Player player)) return;
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;

		MinecraftServer server = level.getServer();
		if (server == null) return;

		// Get the structure name from NBT
		String shipName = nbt.getString("Name");
		if (shipName.isEmpty()) return;

		// Calculate placement position based on player's view
		Vec3 playerPos = player.position();
		double yawRadians = Math.toRadians(player.getYRot());
		double pitchRadians = Math.toRadians(player.getXRot());
		double dx = -Math.sin(yawRadians) * Math.cos(pitchRadians);
		double dy = -Math.sin(pitchRadians);
		double dz = Math.cos(yawRadians) * Math.cos(pitchRadians);

		double targetX = playerPos.x + dx;
		double targetY = playerPos.y + dy + 1;
		double targetZ = playerPos.z + dz;

		BlockPos targetPos = new BlockPos((int)targetX, (int)targetY, (int)targetZ);

		// Load and check the structure
		StructureTemplateManager manager = ((ServerLevel) level).getStructureManager();
		ResourceLocation structureId = new ResourceLocation("bottleship", shipName);
		StructureTemplate template = manager.getOrCreate(structureId);

		if (template != null) {
			// Get structure size
			var templateSize = template.getSize();
			BlockPos size = new BlockPos(templateSize.getX(), templateSize.getY(), templateSize.getZ());
			
			// Check if there's enough space
			boolean hasSpace = true;
			for (int x = 0; x < size.getX(); x++) {
				for (int y = 0; y < size.getY(); y++) {
					for (int z = 0; z < size.getZ(); z++) {
						BlockPos checkPos = targetPos.offset(x, y, z);
						if (!level.getBlockState(checkPos).isAir()) {
							hasSpace = false;
							break;
						}
					}
					if (!hasSpace) break;
				}
				if (!hasSpace) break;
			}

			if (!hasSpace) {
				// Send message to player if there's not enough space
				player.displayClientMessage(Component.translatable("message.bottle_ship.no_space"), true);
				return;
			}

			// Place the structure
			StructurePlaceSettings settings = new StructurePlaceSettings();
			boolean success = template.placeInWorld((ServerLevel) level, targetPos, targetPos, settings, level.random, 2);

			if (success) {				
				// Create DenseBlockPosSet to collect ship blocks
				DenseBlockPosSet blockSet = new DenseBlockPosSet();

				// Collect all non-air blocks in the structure
				for (int x = 0; x < size.getX(); x++) {
					for (int y = 0; y < size.getY(); y++) {
						for (int z = 0; z < size.getZ(); z++) {
							BlockPos relativePos = new BlockPos(x, y, z);
							BlockPos worldPos = targetPos.offset(x, y, z);
							if (!level.getBlockState(worldPos).isAir()) {
								blockSet.add(worldPos.getX(), worldPos.getY(), worldPos.getZ());
							}
						}
					}
				}

				if (!blockSet.isEmpty()) {
					// Create ship from collected blocks
					ServerShip ship = ShipAssemblyKt.createNewShipWithBlocks(
						targetPos.offset(size.getX() / 2, size.getY() / 2, size.getZ() / 2),
						blockSet,
						(ServerLevel) level
					);

					if (ship != null) {
						ship.setSlug(shipName);

						// Play sound effect
						level.playSound(null, targetPos, BOTTLE_EMPTY, PLAYERS, 1.0F, 1.0F);

						// Give empty bottle back
						ItemStack emptyBottle = new ItemStack(BOTTLE_WITHOUT_SHIP.get());
						player.setItemInHand(player.getUsedItemHand(), emptyBottle);
						player.getCooldowns().addCooldown(this, 20);
					}
				}
			}
		}
	}
}
