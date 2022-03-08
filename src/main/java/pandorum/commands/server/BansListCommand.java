package pandorum.commands.server;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class BansListCommand {
    public static void run(final String[] args) {
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
