package darkdustry.utils;

import arc.util.*;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import useful.Bundle;

import static darkdustry.PluginVars.discordServerUrl;
import static darkdustry.utils.Utils.formatDuration;
import static mindustry.Vars.netServer;
import static useful.Bundle.sendToChat;

public class Administration {

    // region kick

    public static void kick(NetConnection con, long duration, boolean showDisclaimer, String key, String locale, Object... values) {
        var reason = Bundle.format(key, locale, values);

        if (duration > 0) reason += Bundle.format("kick.time", locale, formatDuration(duration, locale));
        if (showDisclaimer) reason += Bundle.format("kick.disclaimer", locale, discordServerUrl);
        con.kick(reason, duration);
    }

    public static void kick(NetConnection con, String key, String locale, Object... values) {
        kick(con, 0, false, key, locale, values);
    }

    public static void kick(Player player, long duration, boolean showDisclaimer, String key, Object... values) {
        kick(player.con, duration, showDisclaimer, key, player.locale, values);
    }

    // endregion
    // region administration

    public static void ban(Player admin, Player target, long duration) {
        if (duration == 0) {
            netServer.admins.banPlayerID(target.uuid());
            netServer.admins.banPlayerIP(target.ip());
        } else tempBan(target.uuid(), target.ip(), duration);

        kick(target, duration, true, "kick.banned-by-admin", admin.coloredName());
        Log.info("Player @ has banned @.", admin.plainName(), target.plainName());
        sendToChat("events.admin.ban", admin.coloredName(), target.coloredName());
    }

    public static void kick(Player admin, Player target, long duration) {
        kick(target, duration, true, "kick.kicked-by-admin", admin.coloredName());
        Log.info("Player @ has kicked @.", admin.plainName(), target.plainName());
        sendToChat("events.admin.kick", admin.coloredName(), target.coloredName());
    }

    // endregion
    // region tempBans

    public static void tempBan(String uuid, String ip, long duration) {
        netServer.admins.handleKicked(uuid, ip, duration);
    }

    public static long getTempBanTime(String uuid, String ip) {
        return netServer.admins.getKickTime(uuid, ip) - Time.millis();
    }

    public static boolean isTempBanned(String uuid, String ip) {
        return getTempBanTime(uuid, ip) > 0;
    }
}