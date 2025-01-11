package ForgeStove.BottleShip;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
public class Commands {
	public static void vmodTeleport(long id, MinecraftServer server, int x, int y, int z) {
		executeCommand(String.format("vmod teleport @v[id=%s] %s %s %s (0 0 0)", id, x, y, z), server);
	}
	private static void executeCommand(String command, @NotNull MinecraftServer server) {
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withPermission(4));
		} catch (CommandSyntaxException error) {
			System.err.println("Error executing command: " + error.getMessage());
		}
	}
	public static void vsSetStatic(long id, MinecraftServer server, boolean isStatic) {
		executeCommand(String.format("vs set-static @v[id=%s] %s", id, isStatic), server);
	}
}
