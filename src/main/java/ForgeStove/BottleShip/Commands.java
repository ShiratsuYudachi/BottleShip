package ForgeStove.BottleShip;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.Ship;

import static java.lang.System.err;
public class Commands {
	public static void teleport(
			@NotNull String playerName, @NotNull Ship ship,
			MinecraftServer server, long x, long y, long z
	) {
		executeCommand(
				String.format(
						"execute at %s run vmod teleport %s ~%s ~%s ~%s (0 0 0)", playerName, ship.getSlug(),
						x,
						y,
						z
				), server
		);
	}
	private static void executeCommand(String command, @NotNull MinecraftServer server) {
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withPermission(4));
		} catch (CommandSyntaxException error) {
			err.println("Error executing command: " + error.getMessage());
		}
	}
	public static void setStatic(@NotNull Ship ship, MinecraftServer server, boolean isStatic) {
		executeCommand(String.format("vs set-static %s %s", ship.getSlug(), isStatic), server);
	}
}
