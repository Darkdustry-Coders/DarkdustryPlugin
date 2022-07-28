package pandorum.util;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import pandorum.components.Bundle;

import java.util.Locale;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.util.Search.findLocale;

public class PlayerUtils {

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static void kick(NetConnection con, long duration, boolean showDisclaimer, String key, Locale locale, Object... values) {
       String reason = Bundle.format(key, locale, values);
       if (duration > 0) {
           reason += Bundle.format("kick.time", locale, Utils.formatDuration(duration, locale));
       }

       if (showDisclaimer) {
           reason += Bundle.format("kick.disclaimer", locale, discordServerUrl);
       }

       con.kick(reason, duration);
    }

    public static void kick(NetConnection con, boolean showDisclaimer, String key, Locale locale, Object... values) {
        kick(con, 0, showDisclaimer, key, locale, values);
    }

    public static void kick(Player player, long duration, boolean showDisclaimer, String key, Object... values) {
        kick(player.con, duration, showDisclaimer, key, findLocale(player.locale), values);
    }

    public static void kick(Player player, boolean showDisclaimer, String key, Object... values) {
        kick(player, 0, showDisclaimer, key, values);
    }
}
