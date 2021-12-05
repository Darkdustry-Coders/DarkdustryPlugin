package pandorum.commands.server;

import arc.util.Log;
import pandorum.annotations.commands.ServerCommand;

import static mindustry.Vars.netServer;

public class ConsoleUnbanCommand {
    @ServerCommand(name = "unban", args = "<ip/all/uuid>", description = "Unban a player by ip or uuid.")
    public static void run(final String[] args) {
        if (args[0].equalsIgnoreCase("all")) {
            netServer.admins.getBanned().each(ban -> netServer.admins.unbanPlayerID(ban.id));
            netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
            Log.info("Все игроки разбанены...");
        } else if (netServer.admins.unbanPlayerIP(args[0]) || netServer.admins.unbanPlayerID(args[0])) {
            Log.info("Игрок успешно разбанен: @", args[0]);
        } else {
            Log.err("Этого ip/uuid нет в списке банов!");
        }
    }
}
