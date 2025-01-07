package ForgeStove;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.Ship;

import java.text.DecimalFormat;
public class Commands {
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
