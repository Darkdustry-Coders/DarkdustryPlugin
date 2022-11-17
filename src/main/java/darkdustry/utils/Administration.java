package darkdustry.utils;

import arc.util.Log;
import mindustry.core.UI;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.netServer;
import static useful.Bundle.sendToChat;

public class Administration {

    // region Kick

    public static void kick(NetConnection con, long duration, boolean showDisclaimer, String key, String locale, Object... values) {
        var reason = Bundle.format(key, locale, values);

        if (duration > 0) reason += Bundle.format("kick.time", locale, UI.formatTime(duration * 0.06f));
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
    // region Administration

    public static void ban(Player admin, Player other) {
        netServer.admins.banPlayerID(other.uuid());
        netServer.admins.banPlayerIP(other.ip());

        kick(other, 0, true, "kick.banned-by-admin", admin.coloredName());
        Log.info("Player @ has been banned by @.", other.plainName(), admin.plainName());
        sendToChat("events.admin.ban", admin.coloredName(), other.coloredName());
    }

    public static void kick(Player admin, Player player) {
        kick(player, kickDuration, true, "kick.kicked-by-admin", admin.coloredName());
        Log.info("Player @ has been kicked by @.", player.plainName(), admin.plainName());
        sendToChat("events.admin.kick", admin.coloredName(), player.coloredName());
    }

    // endregion
}