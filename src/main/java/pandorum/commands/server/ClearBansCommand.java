package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.netServer;

public class ClearBansCommand implements ServerCommand {
    public static void run(final String[] args) {
        netServer.admins.getBanned().each(ban -> netServer.admins.unbanPlayerID(ban.id));
        netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
        Log.info("Все игроки разбанены!");
    }
}
