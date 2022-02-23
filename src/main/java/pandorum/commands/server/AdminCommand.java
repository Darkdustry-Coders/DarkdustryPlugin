package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import pandorum.components.Ranks;

import static mindustry.Vars.netServer;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Utils.bundled;

public class AdminCommand {
    public static void run(final String[] args) {
        PlayerInfo info;
        Player target = findPlayer(args[1]);
        if (target != null) {
            info = target.getInfo();
        } else {
            info = netServer.admins.getInfoOptional(args[1]);
            target = Groups.player.find(player -> player.getInfo() == info);
        }

        if (info != null) {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    netServer.admins.adminPlayer(info.id, info.adminUsid);
                    Ranks.setRank(info.id, Ranks.admin);
                    Log.info("Игрок '@' теперь админ.", info.lastName);
                    if (target != null && !target.admin) {
                        target.admin(true);
                        bundled(target, "events.server.admin");
                    }
                }
                case "remove" -> {
                    netServer.admins.unAdminPlayer(info.id);
                    Ranks.resetRank(info.id);
                    Log.info("Игрок '@' больше не админ.", info.lastName);
                    if (target != null && target.admin) {
                        target.admin(false);
                        bundled(target, "events.server.unadmin");
                    }
                }
                default -> Log.err("Второй параметр должен быть или 'add' или 'remove'.");
            }
        } else {
            Log.err("Игрок с таким никнеймом или UUID не найден. Если выдаете права админа по никнейму, убедитесь, что игрок онлайн, иначе используйте его UUID.");
        }
    }
}
