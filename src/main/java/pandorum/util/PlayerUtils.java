package pandorum.util;

import arc.func.Cons;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import pandorum.components.Bundle;

import java.util.Locale;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.discord.Bot.adminRole;
import static pandorum.util.Search.findLocale;

public class PlayerUtils {

    public static boolean isAdmin(Player player) {
        return player != null && player.admin;
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(Permission.ADMINISTRATOR));
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static void kick(NetConnection con, long duration, boolean disclaimer, String key, Locale locale, Object... values) {
       String reason = Bundle.format(key, locale, values);
       if (duration > 0) {
           reason += Bundle.format("kick.time", locale, Utils.formatDuration(duration, locale));
       }

       if (disclaimer) {
           reason += Bundle.format("kick.disclaimer", locale, discordServerUrl);
       }

       con.kick(reason, duration);
    }

    public static void kick(NetConnection con, boolean disclaimer, String key, Locale locale, Object... values) {
        kick(con, 0, disclaimer, key, locale, values);
    }

    public static void kick(Player player, long duration, boolean disclaimer, String key, Object... values) {
        kick(player.con, duration, disclaimer, key, findLocale(player.locale), values);
    }

    public static void kick(Player player, boolean disclaimer, String key, Object... values) {
        kick(player, 0, disclaimer, key, values);
    }

    public static void eachPlayer(Team team, Cons<Player> cons) {
        Groups.player.each(player -> player.team() == team, cons);
    }
}
