package darkdustry.utils;

import arc.files.Fi;
import arc.struct.OrderedMap;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.maps.MapException;
import mindustry.mod.Mods.LoadedMod;
import mindustry.net.NetConnection;
import mindustry.net.WorldReloader;

import java.text.*;
import java.time.*;
import java.util.*;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;

public class Utils {

    public static int voteChoice(String sign) {
        return switch (sign.toLowerCase()) {
            case "y" -> 1;
            case "n" -> -1;
            default -> 0;
        };
    }

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static LoadedMod getPlugin() {
        return mods.getMod("darkdustry-plugin");
    }

    public static Fi getPluginResource(String name) {
        return getPlugin().root.child(name);
    }

    public static String coloredTeam(Team team) {
        return "[#" + team.color + "]" + team.emoji + team.name + "[]";
    }

    public static boolean deepEquals(String first, String second) {
        first = stripAll(first);
        second = stripAll(second);
        return first.equalsIgnoreCase(second) || first.toLowerCase().contains(second.toLowerCase());
    }

    public static String stripAll(String str) {
        return stripColors(stripGlyphs(str));
    }

    public static String formatDate(long time) {
        var format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return format.format(new Date(time));
    }

    public static String formatDuration(long time) {
        return formatDuration(time, defaultLocale);
    }

    public static String formatDuration(long time, Locale locale) {
        var duration = Duration.ofMillis(time);
        var builder = new StringBuilder();
        OrderedMap.<String, Number>of(
                "time.days", duration.toDaysPart(),
                "time.hours", duration.toHoursPart(),
                "time.minutes", duration.toMinutesPart(),
                "time.seconds", duration.toSecondsPart()).each((key, value) -> {
                    if (value.intValue() > 0)
                        builder.append(format(key, locale, value)).append(" ");
        });

        return builder.toString().trim();
    }

    public static void reloadWorld(Runnable load) {
        try {
            var reloader = new WorldReloader();
            reloader.begin();

            load.run();
            state.rules = state.map.applyRules(state.rules.mode());
            logic.play();

            reloader.end();
        } catch (MapException e) {
            Log.err("@: @", e.map.name(), e.getMessage());
            net.closeServer();
        }
    }

    public static void kick(NetConnection con, long duration, boolean showDisclaimer, String key, Locale locale, Object... values) {
        if (!con.isConnected()) { // если игрок по какой-то причине не находится на сервере, просто записываем, что он был кикнут
            netServer.admins.handleKicked(con.uuid, con.address, kickDuration);
            return;
        }

        String reason = format(key, locale, values);
        if (duration > 0) reason += format("kick.time", locale, Utils.formatDuration(duration, locale));
        if (showDisclaimer) reason += format("kick.disclaimer", locale, discordServerUrl);
        con.kick(reason, duration);
    }

    public static void kick(Player player, long duration, boolean showDisclaimer, String key, Object... values) {
        kick(player.con, duration, showDisclaimer, key, Find.locale(player.locale), values);
    }
}
