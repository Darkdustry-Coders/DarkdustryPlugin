package pandorum.commands.server;

import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class PardonCommand implements ServerCommand {
    public static void run(final String[] args) {
        PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
        if (info != null) {
            info.lastKicked = 0;
            if (netServer.admins.kickedIPs.containsKey(info.lastIP)) netServer.admins.kickedIPs.remove(info.lastIP);
            Log.info("Кик снят с игрока: @", info.lastName);
        } else {
            Log.err("Игрок не найден.");
        }
    }
}
