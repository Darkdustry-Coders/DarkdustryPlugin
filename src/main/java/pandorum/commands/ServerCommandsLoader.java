package pandorum.commands;

import arc.Core;
import arc.func.Cons;
import arc.util.CommandHandler;
import pandorum.commands.server.*;

public class ServerCommandsLoader {

    public static void registerServerCommands(CommandHandler handler) {
        registerServer(handler, "help", "List of all commands.", HelpCommand::run);
        registerServer(handler, "exit", "Shut down the server.", ExitCommand::run);
        registerServer(handler, "host", "[map] [mode]", "Open the server. Will default to a random map and survival gamemode if not specified.", HostCommand::run);
        registerServer(handler, "maps", "List of all available maps.", MapsListCommand::run);
        registerServer(handler, "saves", "List of all available saves.", SavesListCommand::run);
        registerServer(handler, "status", "Display server status.", StatusCommand::run);
        registerServer(handler, "say", "<message...>", "Send a message to all players.", SayCommand::run);
        registerServer(handler, "rules", "[remove/add] [name] [value...]", "List, add or remove global rules.", RulesCommand::run);
        registerServer(handler, "config", "[name] [value...]", "Configure server settings.", ConfigCommand::run);
        registerServer(handler, "nextmap", "<map...>", "Set the next map to be played after a gameover. Overrides shuffling.", NextMapCommand::run);
        registerServer(handler, "kick", "<ID/username...>", "Kick a player from the server.", KickCommand::run);
        registerServer(handler, "ban", "<type> <uuid/username/ip...>", "Ban a player by UUID, name or IP.", BanCommand::run);
        registerServer(handler, "bans", "List of all banned IPs and UUIDs.", BansListCommand::run);
        registerServer(handler, "unban", "<uuid/all/ip...>", "Unban a player by UUID or IP.", UnbanCommand::run);
        registerServer(handler, "pardon", "<uuid/ip...>", "Pardon a kicked player.", PardonCommand::run);
        registerServer(handler, "admin", "<add/remove> <uuid/username...>", "Make an online user admin.", AdminCommand::run);
        registerServer(handler, "admins", "List of all admins.", AdminsListCommand::run);
        registerServer(handler, "players", "List of all online players.", PlayersListCommand::run);
        registerServer(handler, "save", "<save...>", "Save game state to a slot.", SaveCommand::run);
        registerServer(handler, "load", "<save...>", "Load a save from a slot.", LoadCommand::run);

        registerServer(handler, "despawn", "Kill all units.", DespawnCommand::run);
        registerServer(handler, "restart", "Restart the server.", RestartCommand::run);
        registerServer(handler, "setrank", "<rank> <ID/username...>", "Set a rank for player.", SetRankCommand::run);
    }

    public static void registerServer(CommandHandler serverHandler, String text, String params, String description, Cons<String[]> runner) {
        serverHandler.register(text, params, description, args -> Core.app.post(() -> runner.get(args)));
    }

    public static void registerServer(CommandHandler serverHandler, String text, String description, Cons<String[]> runner) {
        registerServer(serverHandler, text, "", description, runner);
    }
}
