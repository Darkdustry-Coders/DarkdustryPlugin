package pandorum.commands.server;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class BansListCommand implements Cons<String[]> {
    public void get(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            netServer.admins.getBanned().each(info -> netServer.admins.unbanPlayerID(info.id));
            netServer.admins.getBannedIPs().each(ip -> netServer.admins.unbanPlayerIP(ip));
            Log.info("Список банов очищен.");
            return;
        }

        Seq<PlayerInfo> bannedIDs = netServer.admins.getBanned();
        if (bannedIDs.isEmpty()) {
            Log.info("Не найдено заблокированных игроков... [UUID]");
        } else {
            Log.info("Заблокированные игроки [UUID]: (@)", bannedIDs.size);
            bannedIDs.each(ban -> Log.info("  '@' / Никнейм: '@'", ban.id, ban.lastName));
        }

        Seq<String> bannedIPs = netServer.admins.getBannedIPs();
        if (bannedIPs.isEmpty()) {
            Log.info("Не найдено заблокированных игроков... [IP]");
        } else {
            Log.info("Заблокированные игроки [IP]: (@)", bannedIPs.size);
            bannedIPs.each(ip -> {
                PlayerInfo info = netServer.admins.findByIP(ip);
                if (info != null) {
                    Log.info("  '@' / Никнейм: '@' / UUID: '@'", ip, info.lastName, info.id);
                } else {
                    Log.info("  '@' (Нет информации об игроке)", ip);
                }
            });
        }
    }
}
