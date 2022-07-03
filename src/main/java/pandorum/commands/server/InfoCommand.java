package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class InfoCommand implements Cons<String[]> {
    public void get(String[] args) {
        Seq<PlayerInfo> infos = netServer.admins.findByName(args[0]).asArray();

        if (infos.isEmpty()) {
            Log.info("Не найдено ни одного игрока с таким никнеймом, IP или UUID.");
        } else {
            Log.info("Все найденные игроки: (@)", infos.size);

            int i = 0;
            for (PlayerInfo info : infos) {
                Log.info("  - [@] Информация об игроке '@' / UUID: @", ++i, info.lastName, info.id, info.lastName);
                Log.info("    все использованные никнеймы: @", info.names);
                Log.info("    IP адрес: @", info.lastIP);
                Log.info("    все использованные IP адреса: @", info.ips);
                Log.info("    подключился к серверу: @ раз", info.timesJoined);
                Log.info("    выгнан с сервера: @ раз", info.timesKicked);
            }
        }
    }
}
