package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.netServer;

public class ClearBansCommand {
    public static void run(final String[] args) {
        netServer.admins.getBanned().each(unban -> netServer.admins.unbanPlayerID(unban.id));
        netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
        Log.info("Все игроки разбанены!");
    }
}
