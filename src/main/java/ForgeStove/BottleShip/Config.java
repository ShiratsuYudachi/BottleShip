package ForgeStove.BottleShip;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
public class Config {
	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final Config CONFIG;
	public static ConfigValue<Integer> bottleWithoutShipChargeTime;
	public static ConfigValue<Integer> bottleWithoutShipCooldown;
	public static ConfigValue<Integer> bottleWithShipChargeTime;
	public static ConfigValue<Integer> bottleWithShipCooldown;
	public static ConfigValue<Integer> bottleWithShipChargeStrength;
	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		CONFIG = specPair.getKey();
		CONFIG_SPEC = specPair.getValue();
	}
	public Config(ForgeConfigSpec.@NotNull Builder builder) {
		bottleWithoutShipChargeTime = builder.defineInRange("bottleWithoutShipChargeTime/ms", 1000, 0, 100000);
		bottleWithoutShipCooldown = builder.defineInRange("bottleWithoutShipCooldown/tick", 60, 0, Integer.MAX_VALUE);
		bottleWithShipChargeTime = builder.defineInRange("bottleWithShipChargeTime/ms", 1000, 0, 100000);
		bottleWithShipCooldown = builder.defineInRange("bottleWithShipCooldown/tick", 60, 0, Integer.MAX_VALUE);
		bottleWithShipChargeStrength = builder.defineInRange("bottleWithShipChargeStrength", 5, 0, Integer.MAX_VALUE);
	}
}
