package pandorum.commands.server;

import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import pandorum.features.Ranks;

import static mindustry.Vars.netServer;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findPlayerInfo;
import static pandorum.util.Utils.bundled;

public class AdminCommand implements Cons<String[]> {
    public void get(String[] args) {
        PlayerInfo info = findPlayerInfo(args[1]);
        Player target = findPlayer(args[1]);
        if (info == null) {
            Log.err("Игрок '@' не найден...", args[1]);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                netServer.admins.adminPlayer(info.id, info.adminUsid);
                Ranks.setRank(info.id, Ranks.admin);
                Log.info("Игрок '@' теперь админ.", info.lastName);
                if (target != null) {
                    target.admin(true);
                    bundled(target, "events.server.admin");
                }
            }

            case "remove" -> {
                netServer.admins.unAdminPlayer(info.id);
                Ranks.setRank(info.id, Ranks.player);
                Log.info("Игрок '@' больше не админ.", info.lastName);
                if (target != null) {
                    target.admin(false);
                    bundled(target, "events.server.unadmin");
                }
            }

            default -> Log.err("Первый параметр команды должен быть или 'add' или 'remove'.");
        }
    }
}
