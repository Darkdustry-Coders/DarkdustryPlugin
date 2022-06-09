package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class UnbanCommand implements Cons<String[]> {
    public void get(String[] args) {
        PlayerInfo info;
        if ((info = netServer.admins.getInfoOptional(args[0])) == null && (info = netServer.admins.findByIP(args[0])) == null) {
            Log.err("Игрок '@' не найден...", args[0]);
            return;
        }

        netServer.admins.unbanPlayerID(info.id);
        netServer.admins.unbanPlayerIP(info.lastIP);
        Log.info("Игрок '@' успешно разбанен.", info.lastName);
    }
}
