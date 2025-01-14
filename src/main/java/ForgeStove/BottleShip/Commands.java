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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
public class Commands {
	public static void vmodTeleport(
			@NotNull String playerName,
			long shipID,
			MinecraftServer server,
			int x,
			int y,
			int z
	) {
		executeCommand(
				String.format(
						"execute at %s run vmod teleport @v[id=%s] ~%s ~%s ~%s (0 0 0)",
						playerName.substring(8, playerName.length() - 1),
						shipID,
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
			System.err.println("Error executing command: " + error.getMessage());
		}
	}
	public static void vsSetStatic(long id, MinecraftServer server, boolean isStatic) {
		executeCommand(String.format("vs set-static @v[id=%s] %s", id, isStatic), server);
	}
}
