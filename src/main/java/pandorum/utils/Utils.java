package pandorum.utils;

import arc.Core;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.server.ServerControl;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;
import pandorum.struct.Tuple2;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.port;
import static pandorum.PluginVars.config;

public class Utils {

    public static String colorizedTeam(Team team) {
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static boolean adminCheck(Player player) {
        return player.admin;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, Search.findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static String formatTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(time);
    }

    public static long millisecondsToMinutes(long duration) {
        return TimeUnit.MILLISECONDS.toMinutes(duration);
    }

    public static long secondsToMinutes(long duration) {
        return TimeUnit.SECONDS.toMinutes(duration);
    }

    public static Tuple2<String, Integer> hubIp() {
        String[] parts = config.hubIp.split(":");
        return parts.length > 1 ? Tuple2.of(parts[0], Strings.parseInt(parts[1], port)) : Tuple2.of(config.hubIp, port);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }
}
