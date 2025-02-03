package ForgeStove.BottleShip;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.NotNull;

import static ForgeStove.BottleShip.BottleShip.MOD_ID;
import static ForgeStove.BottleShip.Config.CONFIG_SPEC;
import static net.minecraft.network.chat.Component.*;
import static net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL;
import static net.minecraft.world.item.CreativeModeTab.builder;
import static net.minecraft.world.item.Item.Properties;
import static net.minecraft.world.item.Rarity.UNCOMMON;
import static net.minecraftforge.fml.config.ModConfig.Type.SERVER;
import static net.minecraftforge.registries.DeferredRegister.create;
@Mod(MOD_ID) public class BottleShip {
	public static final String MOD_ID = "bottle_ship";
	public static final DeferredRegister<CreativeModeTab> TABS;
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP;
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP;
	public static final RegistryObject<CreativeModeTab> ITEM_TAB;
	public static final DeferredRegister<Item> ITEMS;
	static {
		TABS = create(Registries.CREATIVE_MODE_TAB, MOD_ID);
		ITEMS = create(ForgeRegistries.ITEMS, MOD_ID);
		BOTTLE_WITHOUT_SHIP = ITEMS.register(
				"bottle_without_ship",
				() -> new BottleWithoutShipItem(new Properties().stacksTo(1))
		);
		BOTTLE_WITH_SHIP = ITEMS.register(
				"bottle_with_ship",
				() -> new BottleWithShipItem(new Properties().stacksTo(1).rarity(UNCOMMON).fireResistant())
		);
		ITEM_TAB = TABS.register(
				"tab." + MOD_ID, () -> builder().title(translatable("tab." + MOD_ID))
						.icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
						.displayItems((parameters, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get()))
						.build()
		);
	}
	public BottleShip(@NotNull FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		context.registerConfig(SERVER, CONFIG_SPEC);
		ITEMS.register(modEventBus);
		TABS.register(modEventBus);
	}
	public static void onUseTickCommon(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			int tickCount,
			int chargeTime
	) {
		if (!level.isClientSide()) return;
		if (!(livingEntity instanceof Player player)) return;
		int progress = tickCount * 1000 / chargeTime;
		if (progress == 18) player.playSound(NOTE_BLOCK_BELL.value(), 5.0F, 5.0F);
		StringBuilder progressBar = new StringBuilder("§f[");
		for (int i = 0; i < 18; i++) {
			if (i < progress) progressBar.append("§a■");
			else progressBar.append("§c■");
		}
		progressBar.append("§f]");
		player.displayClientMessage(literal(progressBar.toString()), true);
	}
}
