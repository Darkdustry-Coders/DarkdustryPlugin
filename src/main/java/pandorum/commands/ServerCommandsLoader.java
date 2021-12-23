package pandorum.commands;

import arc.util.CommandHandler;
import pandorum.commands.server.*;

public class ServerCommandsLoader {

    public static void registerServerCommands(CommandHandler handler) {
        CommandsHelper.register(handler, "despawn", "Kill all units.", DespawnCommand::run);
        CommandsHelper.register(handler, "restart", "Restart the server.", RestartCommand::run);
        CommandsHelper.register(handler, "exit", "Shut down the server.", ExitCommand::run);
        CommandsHelper.register(handler, "say", "<message...>", "Send a message as a server..", SayCommand::run);
        CommandsHelper.register(handler, "kick", "<player...>", "Kick a player from the server.", KickCommand::run);
        CommandsHelper.register(handler, "pardon", "<uuid...>", "Pardon a kicked player.", PardonCommand::run);
        CommandsHelper.register(handler, "ban", "<ip/name/id> <ip/username/uuid...>", "Ban a player by ip, name or uuid.", BanCommand::run);
        CommandsHelper.register(handler, "unban", "<ip/all/uuid...>", "Unban a player by ip or uuid.", UnbanCommand::run);
    }
}
