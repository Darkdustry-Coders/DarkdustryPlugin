package pandorum.commands.server;

import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class PardonCommand {
    public static void run(final String[] args) {
        PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
        if (info == null) {
            Log.err("Игрок не найден...");
            return;
        }

        info.lastKicked = 0;
        info.ips.each(netServer.admins.kickedIPs::remove);
        Log.info("Игрок @ снова может зайти на сервер.", info.lastName);
    }
}
