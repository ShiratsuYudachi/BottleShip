package ForgeStove.BottleShip;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.*;
@Mod(BottleShip.MODID) public class BottleShip {
	public static final String MODID = "bottle_ship";
	public static final Map<Long, ShipData> SHIPS = new HashMap<>();
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