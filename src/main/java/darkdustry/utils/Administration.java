package darkdustry.utils;

import arc.util.Log;
import mindustry.core.UI;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import useful.Bundle;

import static arc.util.Strings.stripColors;
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

    public static void ban(Player player, String admin) {
        netServer.admins.banPlayerID(player.uuid());
        netServer.admins.banPlayerIP(player.ip());

        kick(player, 0, true, "kick.banned-by-admin", admin);
        Log.info("Player @ has been banned by @.", player.plainName(), stripColors(admin));
        sendToChat("events.admin.ban", admin, player.coloredName());
    }

    public static void kick(Player player, String admin) {
        kick(player, kickDuration, true, "kick.kicked-by-admin", admin);
        Log.info("Player @ has been kicked by @.", player.plainName(), stripColors(admin));
        sendToChat("events.admin.kick", admin, player.coloredName());
    }

    // endregion
}