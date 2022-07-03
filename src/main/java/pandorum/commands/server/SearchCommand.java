package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class SearchCommand implements Cons<String[]> {
    public void get(String[] args) {
        Seq<PlayerInfo> infos = netServer.admins.searchNames(args[0]).asArray();

        if (infos.isEmpty()) {
            Log.info("Не найдено ни одного игрока с таким никнеймом.");
        } else {
            Log.info("Найдено @ игроков:", infos.size);

            for (int i = 0; i < infos.size; i++) {
                PlayerInfo info = infos.get(i);
                Log.info("- [@] '@' / UUID: @", i++, info.lastName, info.id);
            }
        }
    }
}
