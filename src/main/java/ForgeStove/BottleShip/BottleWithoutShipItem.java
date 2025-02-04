package ForgeStove.BottleShip;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;

import static ForgeStove.BottleShip.BottleShip.BOTTLE_WITH_SHIP;
import static ForgeStove.BottleShip.BottleShip.onUseTickCommon;
import static ForgeStove.BottleShip.Commands.*;
import static ForgeStove.BottleShip.Config.*;
import static net.minecraft.world.InteractionResult.CONSUME;
import static net.minecraft.world.InteractionResult.FAIL;
import static net.minecraft.world.InteractionResult.SUCCESS;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;

/**
 * Forge-based BottleWithoutShip item that captures a Valkyrien Skies ship as NBT
 * (via StructureTemplate) similar to your Fabric snippet.
 */
public class BottleWithoutShipItem extends Item {

	// We'll preserve this for the 'useOn()' and charging approach
	private UseOnContext context;

	public BottleWithoutShipItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		Player player = useOnContext.getPlayer();
		if (level.isClientSide() || player == null || player instanceof FakePlayer) {
			return FAIL;
		}
		// Start "charging" the bottle usage
		context = useOnContext;
		player.startUsingItem(useOnContext.getHand());
		return CONSUME;
	}

	@Override
	public void onUseTick(@NotNull Level level,
						  @NotNull LivingEntity livingEntity,
						  @NotNull ItemStack itemStack,
						  int tickLeft) {
		// If you want some client-side swirl or progress indicator, keep this:
		if (!level.isClientSide()) {
			return;
		}
		// Example: could show a progress bar or do particles
		onUseTickCommon(level, livingEntity, getUseDuration(itemStack) - tickLeft,
				bottleWithoutShipChargeTime.get());
	}

	@Override
	public int getUseDuration(@NotNull ItemStack itemStack) {
		// Large value to allow you to hold it down for a while
		return 100000;
	}

	/**
	 * Called when player stops using the item (i.e. releases right-click).
	 */
	@Override
	public void releaseUsing(@NotNull ItemStack stack,
							 @NotNull Level level,
							 @NotNull LivingEntity livingEntity,
							 int timeLeft) {

		// We only care about server-side logic for capturing the structure
		if (level.isClientSide()) {
			return;
		}
		// The actual time spent “charging”
		long timeChargedMs = ((long) (getUseDuration(stack) - timeLeft)) * 1000L / 20L;
		if (timeChargedMs < bottleWithoutShipChargeTime.get()) {
			// Not enough “draw time” => do nothing
			return;
		}
		if (!(livingEntity instanceof Player player)) {
			return;
		}
		if (context == null) {
			return;
		}

		// Retrieve the block pos where the player used the item
		BlockPos clickedPos = context.getClickedPos();
		// Check for the ship
		ServerShip ship = getShipManagingPos((ServerLevel) level, clickedPos);
		if (ship == null) {
			// No ship => do nothing
			return;
		}

		// XP check or creative
		if (!player.isCreative() && player.experienceLevel < 30) {
			// Example feedback for insufficient XP
			player.displayClientMessage(
					net.minecraft.network.chat.Component.literal("Not enough levels."),
					false
			);
			return;
		}

		// Build the new bottle-with-ship item
		ItemStack newStack = new ItemStack(BOTTLE_WITH_SHIP.get());
		CompoundTag newTag = new CompoundTag();
		// Store the ship ID or name
		long id = ship.getId();
		newTag.putString("ID", String.valueOf(id));
		// If you prefer using slug:
		if (ship.getSlug() != null) {
			newTag.putString("Name", ship.getSlug());
		}
		// Figure out bounding box from ship
		AABBic aabb = ship.getShipAABB();
		if (aabb == null) {
			return;
		}
		int minX = aabb.minX(), minY = aabb.minY(), minZ = aabb.minZ();
		int maxX = aabb.maxX(), maxY = aabb.maxY(), maxZ = aabb.maxZ();

		// Save some size info in the tag just for reference
		int sizeX = maxX - minX;
		int sizeY = maxY - minY;
		int sizeZ = maxZ - minZ;
		newTag.putString(
				"Size",
				String.format("[X:%d Y:%d Z:%d]", sizeX, sizeY, sizeZ)
		);

		// The main difference: we collect the blocks into a StructureTemplate
		MinecraftServer server = level.getServer();
		if (server == null) return;

		StructureTemplateManager manager = ((ServerLevel) level).getStructureManager();
		// ResourceLocation for the structure
		ResourceLocation structureID = new ResourceLocation("bottleship", // or your modid
				ship.getSlug() == null ? "placeholder"
						: ship.getSlug());
		// Create or get the template
		StructureTemplate template = manager.getOrCreate(structureID);

		// Decide where to start scanning: we can just use minX/minY/minZ as the origin
		BlockPos structureStart = new BlockPos(minX, minY, minZ);
		// The total bounding box size ( +1 to be inclusive if that’s your intention)
		net.minecraft.core.Vec3i dimension = new net.minecraft.core.Vec3i(
				sizeX + 1,
				sizeY + 1,
				sizeZ + 1
		);

		// Fill from world => “saveFromWorld” in Yarn is “fillFromWorld” in official
		// The final parameter is the block to ignore if you only want to skip certain blocks, or null
		template.fillFromWorld((ServerLevel) level,
				structureStart,
				dimension,
				true, // include entities?
				Blocks.AIR  // ignore air blocks or skip some block type
		);

		// Now actually save the template to disk
		manager.save(structureID);

		// Next, remove the blocks from the world (like the original Fabric snippet)
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!level.isEmptyBlock(pos)) {
						// Remove any block entity
						if (level.getBlockEntity(pos) != null) {
							level.removeBlockEntity(pos);
						}
						// Replace with air
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						// Optionally spawn some fancy particle
						if (RandomSource.create().nextDouble() > 0.7) {
							((ServerLevel) level).sendParticles(
									ParticleTypes.END_ROD,
									x + 0.5,
									y + 0.5,
									z + 0.5,
									1,
									0.0, 0.0, 0.0,
									0.0
							);
						}
					}
				}
			}
		}

		// Play a sound for feedback
		level.playSound(null,
				clickedPos,
				SoundEvents.EVOKER_PREPARE_ATTACK,
				SoundSource.PLAYERS,
				1.0F,
				1.0F);

		// Deduct XP if not creative
		if (!player.isCreative()) {
			player.giveExperienceLevels(-30);
		}

		// Put the new bottle-with-ship stack in the player's hand
		newStack.setTag(newTag);
		player.setItemInHand(player.getUsedItemHand(), newStack);

		// Add cooldown
		player.getCooldowns().addCooldown(newStack.getItem(), bottleWithoutShipCooldown.get());
		// Finally, play the "fill bottle" sound
		level.playSound(null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.BOTTLE_FILL,
				SoundSource.PLAYERS,
				1.0F,
				1.0F);
	}

	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		// Just for visuals—using the "bow" animation as an example
		return UseAnim.BOW;
	}
}
