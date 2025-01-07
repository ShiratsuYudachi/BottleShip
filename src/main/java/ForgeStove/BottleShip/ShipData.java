package ForgeStove.BottleShip;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.Ship;
public class ShipData {
	public Ship ship;
	public Level level;
	public ShipData(Ship ship, Level level) {
		this.level = level;
		this.ship = ship;
	}
}