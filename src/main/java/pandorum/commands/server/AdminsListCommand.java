package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;
import pandorum.features.Ranks;

import static mindustry.Vars.netServer;

public class AdminsListCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            netServer.admins.getAdmins().each(info -> {
                netServer.admins.unAdminPlayer(info.id);
                Ranks.setRank(info.id, Ranks.player);
            });
            // TODO сообщение в консоль о том, что все админки сняты
            return;
        }

        Seq<PlayerInfo> admins = netServer.admins.getAdmins();
        if (admins.isEmpty()) {
            Log.info("Не найдено администраторов.");
        } else {
            Log.info("Администраторы: (@)", admins.size);
            admins.each(admin -> Log.info("  &lm'@' /  UUID: '@' / IP: '@'", admin.lastName, admin.id, admin.lastIP));
        }
    }
}
