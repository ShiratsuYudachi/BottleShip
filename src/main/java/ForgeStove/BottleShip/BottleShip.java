package ForgeStove.BottleShip;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import static ForgeStove.BottleShip.Config.CONFIG_SPEC;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.world.item.CreativeModeTab.builder;
import static net.minecraft.world.item.Item.Properties;
import static net.minecraft.world.item.Rarity.UNCOMMON;
import static net.minecraftforge.fml.config.ModConfig.Type.COMMON;
import static net.minecraftforge.registries.DeferredRegister.create;
@Mod(BottleShip.MODID) public class BottleShip {
	public static final String MODID = "bottle_ship";
	public static final DeferredRegister<CreativeModeTab> TABS;
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP;
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP;
	public static final RegistryObject<CreativeModeTab> ITEM_TAB;
	public static final DeferredRegister<Item> ITEMS;
	static {
		TABS = create(Registries.CREATIVE_MODE_TAB, MODID);
		ITEMS = create(ForgeRegistries.ITEMS, MODID);
		BOTTLE_WITHOUT_SHIP = ITEMS.register(
				"bottle_without_ship", () -> new BottleWithoutShipItem(new Properties().stacksTo(1))
		);
		BOTTLE_WITH_SHIP = ITEMS.register(
				"bottle_with_ship",
				() -> new BottleWithShipItem(new Properties().stacksTo(1).rarity(UNCOMMON).fireResistant())
		);
		ITEM_TAB = TABS.register(
				"tab." + MODID, () -> builder().title(translatable("tab." + MODID))
						.icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
						.displayItems((parameters, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get()))
						.build()
		);
	}
	public BottleShip(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		context.registerConfig(COMMON, CONFIG_SPEC);
		ITEMS.register(modEventBus);
		TABS.register(modEventBus);
	}
}
