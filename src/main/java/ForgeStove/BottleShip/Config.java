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
		bottleWithoutShipChargeTime = builder.defineInRange(
				"bottleWithoutShipChargeTime/ms",
				1000,
				0, 100000
		);
		bottleWithoutShipCooldown = builder.defineInRange("bottleWithoutShipCooldown/tick", 100, 0, Integer.MAX_VALUE);
		bottleWithShipChargeTime = builder.defineInRange("bottleWithShipChargeTime/ms", 1000, 0, 100000);
		bottleWithShipCooldown = builder.defineInRange("bottleWithShipCooldown/tick", 100, 0, Integer.MAX_VALUE);
		bottleWithShipChargeStrength = builder.defineInRange("bottleWithShipChargeStrength", 5, 0, Integer.MAX_VALUE);
	}
}
