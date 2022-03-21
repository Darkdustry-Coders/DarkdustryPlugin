package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class PardonCommand implements Cons<String[]> {
    public void get(String[] args) {
        PlayerInfo info;
        if ((info = netServer.admins.getInfoOptional(args[0])) == null && (info = netServer.admins.findByIP(args[0])) == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        info.lastKicked = 0L;
        info.ips.each(netServer.admins.kickedIPs::remove);
        Log.info("Игрок '@' снова может зайти на сервер.", info.lastName);
    }
}
