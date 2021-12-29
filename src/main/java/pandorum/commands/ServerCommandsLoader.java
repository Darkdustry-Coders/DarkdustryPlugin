package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.server.*;

public class ServerCommandsLoader {

    public static void registerServerCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "despawn", "Kill all units.", DespawnCommand::run);
        CommandsHelper.register(handler, "restart", "Restart the server.", RestartCommand::run);
        CommandsHelper.register(handler, "exit", "Shut down the server.", ExitCommand::run);
        CommandsHelper.register(handler, "say", "<message...>", "Send a message as a server..", SayCommand::run);
        CommandsHelper.register(handler, "kick", "<ID/username...>", "Kick a player from the server.", KickCommand::run);
        CommandsHelper.register(handler, "pardon", "<uuid...>", "Pardon a kicked player.", PardonCommand::run);
        CommandsHelper.register(handler, "ban", "<type> <uuid/username/ip...>", "Ban a player by uuid, name or ip.", BanCommand::run);
        CommandsHelper.register(handler, "unban", "<uuid/all/ip...>", "Unban a player by uuid or ip.", UnbanCommand::run);
        CommandsHelper.register(handler, "admin", "<add/remove> <uuid/username...>", "Make an online user admin.", AdminCommand::run);
    }
}
