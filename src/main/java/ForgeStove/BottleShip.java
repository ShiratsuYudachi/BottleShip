package ForgeStove;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.*;

import java.text.DecimalFormat;
import java.util.*;

import static ForgeStove.BottleShip.SHIPS;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;
@Mod(BottleShip.MODID) public class BottleShip {
	public static final String MODID = "bottle_ship";
	public static final Map<Long, Ship> SHIPS = new HashMap<>();
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
			Registries.CREATIVE_MODE_TAB,
			MODID
	);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP = ITEMS.register(
			"bottle_without_ship",
			() -> new BottleWithoutShip(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
	);
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP = ITEMS.register(
			"bottle_with_ship",
			() -> new BottleWithShip(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
	);
	public static final RegistryObject<CreativeModeTab> ITEM_TAB = TABS.register(
			"tab." + MODID,
			() -> CreativeModeTab.builder()
					.title(Component.translatable("tab." + MODID))
					.icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
					.displayItems((parameters, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get()))
					.build()
	);
	public BottleShip() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		TABS.register(modEventBus);
		if (ITEM_TAB == null) System.err.println("Failed to create item tab");
	}
}
class BottleWithoutShip extends Item {
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
		SHIPS.put(ship.getId(), ship);
		AABBdc shipAABB = ship.getWorldAABB();
		MinecraftServer server = level.getServer();
		Commands.vsTeleport(
				ship, server, blockPos.getX(),
				blockPos.getY() + Objects.requireNonNull(shipAABB).maxY(),
				blockPos.getZ()
		);
		if (!((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship, server, true);
		ItemStack newItemStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		CompoundTag nbt = newItemStack.getOrCreateTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		nbt.putString("Name", Objects.requireNonNull(ship.getSlug()));
		nbt.putString("Size", String.valueOf(shipAABB));
		newItemStack.setTag(nbt);
		player.getInventory().removeItem(context.getItemInHand());
		player.addItem(newItemStack);
		return InteractionResult.CONSUME;
	}
}
class BottleWithShip extends Item {
	public BottleWithShip(Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(
			@NotNull ItemStack itemStack,
			Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag
	) {
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(Component.nullToEmpty(nbt.getString("ID")));
		tooltip.add(Component.nullToEmpty(nbt.getString("Name")));
		tooltip.add(Component.nullToEmpty(nbt.getString("Size")));
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
		Ship ship = SHIPS.get(id);
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
					new BlockPos((int) (targetX - (depth / 2)), (int) targetY, (int) (targetZ - (depth / 2))),
					new BlockPos((int) (targetX + (depth / 2)), (int) (targetY + 1), (int) (targetZ + (depth / 2)))
			)) {
				if (!level.getBlockState(pos).isAir()) {
					hasBlock = true;
					break;
				}
			}
			if (!hasBlock) break;
			targetY = Math.ceil(targetY) + 1;
		}
		MinecraftServer server = level.getServer();
		Commands.vsTeleport(ship, server, targetX, targetY, targetZ);
		if (((ServerShip) ship).isStatic()) Commands.vsSetStatic(ship, server, false);
		SHIPS.remove(id);
		return InteractionResultHolder.success(new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get()));
	}
}
class Commands {
	public static void vsTeleport(@NotNull Ship ship, MinecraftServer server, double x, double y, double z) {
		DecimalFormat decimalFormat = new DecimalFormat("0.#");
		String command = String.format(
				"vs teleport @v[id=%s] %s %s %s",
				ship.getId(),
				decimalFormat.format(x),
				decimalFormat.format(y),
				decimalFormat.format(z)
		);
		executeCommand(command, server);
	}
	private static void executeCommand(String command, @NotNull MinecraftServer server) {
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withPermission(2));
		} catch (CommandSyntaxException error) {
			System.err.println("Error executing command: " + error.getMessage());
		}
	}
	public static void vsSetStatic(@NotNull Ship ship, MinecraftServer server, boolean isStatic) {
		String command = String.format("vs set-static @v[id=%s] %s", ship.getId(), isStatic);
		executeCommand(command, server);
	}
}