package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.discord.*;

public class DiscordCommandsLoader {

    public static void registerDiscordCommands(CommandHandler handler) {
        CommandsHelper.registerDiscord(handler, "help", "List of all commands.", false, HelpCommand::run);
        CommandsHelper.registerDiscord(handler, "ip", "IP address of this server.", false, IpCommand::run);
        CommandsHelper.registerDiscord(handler, "addmap", "Add a map to the server.", true, AddMapCommand::run);
        CommandsHelper.registerDiscord(handler, "map", "<name...>", "Get a map from the server.", false, MapCommand::run);
        CommandsHelper.registerDiscord(handler, "removemap", "<name...>", "Delete a map from the server.", true, RemoveMapCommand::run);
        CommandsHelper.registerDiscord(handler, "maps", "[page]", "List of all maps.", false, MapsListCommand::run);
        CommandsHelper.registerDiscord(handler, "players", "[page]", "List of all online players.", false, PlayersListCommand::run);
        CommandsHelper.registerDiscord(handler, "status", "See server status.", false, StatusCommand::run);
    }
}
