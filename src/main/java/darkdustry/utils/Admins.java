package darkdustry.utils;

import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import useful.Bundle;

import static darkdustry.PluginVars.discordServerUrl;
import static darkdustry.utils.Utils.formatDuration;
import static mindustry.Vars.netServer;

public class Admins {

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
        ban(target.uuid(), target.ip(), duration);

        kick(target, duration, true, "kick.banned-by-admin", admin.coloredName());
        Log.info("Player @ has banned @.", admin.plainName(), target.plainName());
        Bundle.send("events.admin.ban", admin.coloredName(), target.coloredName());
    }

    public static void kick(Player admin, Player target, long duration) {
        kick(target, duration, true, "kick.kicked-by-admin", admin.coloredName());
        Log.info("Player @ has kicked @.", admin.plainName(), target.plainName());
        Bundle.send("events.admin.kick", admin.coloredName(), target.coloredName());
    }

    // endregion
    // region bans

    public static void ban(String uuid, String ip, long duration) {
        if (duration == 0) {
            netServer.admins.banPlayerID(uuid);
            netServer.admins.banPlayerIP(ip);
        } else netServer.admins.handleKicked(uuid, ip, duration);
    }

    public static long getBanTime(String uuid, String ip) {
        return netServer.admins.getKickTime(uuid, ip) - Time.millis();
    }

    public static boolean isBanned(String uuid, String ip) {
        return getBanTime(uuid, ip) > 0;
    }

    // endregion
}