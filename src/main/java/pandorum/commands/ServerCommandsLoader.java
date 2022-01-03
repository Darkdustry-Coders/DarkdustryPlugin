package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.server.*;

public class ServerCommandsLoader {

    public static void registerServerCommands(CommandHandler handler) {
        CommandsHelper.remove(handler, "mod");
        CommandsHelper.remove(handler, "pause");
        CommandsHelper.remove(handler, "fillitems");

        CommandsHelper.register(handler, "version", "Displays server version info.", VersionCommand::run);
        CommandsHelper.register(handler, "exit", "Shut down the server.", ExitCommand::run);
        CommandsHelper.register(handler, "stop", "Stop hosting the server.", StopCommand::run);
        CommandsHelper.register(handler, "host", "[mode] [map...]", "Open the server. Will default to a random map and survival gamemode if not specified.", HostCommand::run);
        CommandsHelper.register(handler, "maps", "List of all available maps.", MapsListCommand::run);
        CommandsHelper.register(handler, "reloadmaps", "Reload all maps from disk.", ReloadMapsCommand::run);
        CommandsHelper.register(handler, "status", "Display server status.", StatusCommand::run);
        CommandsHelper.register(handler, "mods", "Display all loaded mods.", ModsListCommand::run);
        CommandsHelper.register(handler, "js", "<script...>", "Run arbitrary Javascript.", JavaScriptCommand::run);
        CommandsHelper.register(handler, "say", "<message...>", "Send a message to all players.", SayCommand::run);
        CommandsHelper.register(handler, "rules", "[remove/add] [name] [value...]", "List, add or remove global rules.", RulesCommand::run);
        CommandsHelper.register(handler, "config", "[name] [value...]", "Configure server settings.", ConfigCommand::run);
        CommandsHelper.register(handler, "nextmap", "<map...>", "Set the next map to be played after a gameover. Overrides shuffling.", NextMapCommand::run);
        CommandsHelper.register(handler, "kick", "<ID/username...>", "Kick a player from the server.", KickCommand::run);
        CommandsHelper.register(handler, "ban", "<type> <uuid/username/ip...>", "Ban a player by UUID, name or IP.", BanCommand::run);
        CommandsHelper.register(handler, "bans", "List of all banned IPs and UUIDs.", BansListCommand::run);
        CommandsHelper.register(handler, "unban", "<uuid/all/ip...>", "Unban a player by UUID or IP.", UnbanCommand::run);
        CommandsHelper.register(handler, "pardon", "<uuid...>", "Pardon a kicked player.", PardonCommand::run);
        CommandsHelper.register(handler, "admin", "<add/remove> <uuid/username...>", "Make an online user admin.", AdminCommand::run);
        CommandsHelper.register(handler, "admins", "List of all admins.", AdminsListCommand::run);
        CommandsHelper.register(handler, "players", "List of all online players.", PlayersListCommand::run);

        CommandsHelper.register(handler, "despawn", "Kill all units.", DespawnCommand::run);
        CommandsHelper.register(handler, "restart", "Restart the server.", RestartCommand::run);
    }
}
