package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class SearchCommand implements Cons<String[]> {
    public void get(String[] args) {
        Seq<PlayerInfo> infos = netServer.admins.searchNames(args[0]).toSeq();

        if (infos.isEmpty()) {
            Log.info("Не найдено ни одного игрока с таким никнеймом.");
        } else {
            Log.info("Все найденные игроки: (@)", infos.size);

            int i = 0;
            for (PlayerInfo info : infos) {
                Log.info("  - [@] '@' / UUID: @", ++i, info.lastName, info.id);
            }
        }
    }
}
