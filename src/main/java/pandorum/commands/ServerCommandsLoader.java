package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.server.*;

public class ServerCommandsLoader {

    public static void registerServerCommands(CommandHandler handler) {
        CommandsHelper.removeCommand(handler, "mod");
        CommandsHelper.removeCommand(handler, "pause");
        CommandsHelper.removeCommand(handler, "fillitems");

        CommandsHelper.registerServer(handler, "help", "List of all commands.", HelpCommand::run);
        CommandsHelper.registerServer(handler, "exit", "Shut down the server.", ExitCommand::run);
        CommandsHelper.registerServer(handler, "host", "[map] [mode]", "Open the server. Will default to a random map and survival gamemode if not specified.", HostCommand::run);
        CommandsHelper.registerServer(handler, "maps", "List of all available maps.", MapsListCommand::run);
        CommandsHelper.registerServer(handler, "saves", "List of all available saves.", SavesListCommand::run);
        CommandsHelper.registerServer(handler, "status", "Display server status.", StatusCommand::run);
        CommandsHelper.registerServer(handler, "say", "<message...>", "Send a message to all players.", SayCommand::run);
        CommandsHelper.registerServer(handler, "rules", "[remove/add] [name] [value...]", "List, add or remove global rules.", RulesCommand::run);
        CommandsHelper.registerServer(handler, "config", "[name] [value...]", "Configure server settings.", ConfigCommand::run);
        CommandsHelper.registerServer(handler, "nextmap", "<map...>", "Set the next map to be played after a gameover. Overrides shuffling.", NextMapCommand::run);
        CommandsHelper.registerServer(handler, "kick", "<ID/username...>", "Kick a player from the server.", KickCommand::run);
        CommandsHelper.registerServer(handler, "ban", "<type> <uuid/username/ip...>", "Ban a player by UUID, name or IP.", BanCommand::run);
        CommandsHelper.registerServer(handler, "bans", "List of all banned IPs and UUIDs.", BansListCommand::run);
        CommandsHelper.registerServer(handler, "unban", "<uuid/all/ip...>", "Unban a player by UUID or IP.", UnbanCommand::run);
        CommandsHelper.registerServer(handler, "pardon", "<uuid...>", "Pardon a kicked player.", PardonCommand::run);
        CommandsHelper.registerServer(handler, "admin", "<add/remove> <uuid/username...>", "Make an online user admin.", AdminCommand::run);
        CommandsHelper.registerServer(handler, "admins", "List of all admins.", AdminsListCommand::run);
        CommandsHelper.registerServer(handler, "players", "List of all online players.", PlayersListCommand::run);
        CommandsHelper.registerServer(handler, "save", "<save...>", "Save game state to a slot.", SaveCommand::run);
        CommandsHelper.registerServer(handler, "load", "<save...>", "Load a save from a slot.", LoadCommand::run);

        CommandsHelper.registerServer(handler, "despawn", "Kill all units.", DespawnCommand::run);
        CommandsHelper.registerServer(handler, "restart", "Restart the server.", RestartCommand::run);
    }
}
