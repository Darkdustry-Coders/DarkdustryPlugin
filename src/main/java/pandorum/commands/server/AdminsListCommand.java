package pandorum.commands.server;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class AdminsListCommand {
    public static void run(final String[] args) {
        Seq<PlayerInfo> admins = netServer.admins.getAdmins();
        if (admins.isEmpty()) {
            Log.info("Не найдено администраторов.");
        } else {
            Log.info("Администраторы: (@)", admins.size);
            admins.each(admin -> Log.info(" &lm @ /  UUID: '@' / IP: '@'", admin.lastName, admin.id, admin.lastIP));
        }
    }
}
