package ForgeStove.BottleShip;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
public class Commands {
	public static void vsTeleport(@NotNull Long id, MinecraftServer server, long x, long y, long z) {
		executeCommand(String.format("vs teleport @v[id=%s] %s %s %s", id, x, y, z), server);
	}
	private static void executeCommand(String command, @NotNull MinecraftServer server) {
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withPermission(2));
		} catch (CommandSyntaxException error) {
			System.err.println("Error executing command: " + error.getMessage());
		}
	}
	public static void vsSetStatic(@NotNull Long id, MinecraftServer server, boolean isStatic) {
		executeCommand(String.format("vs set-static @v[id=%s] %s", id, isStatic), server);
	}
}
