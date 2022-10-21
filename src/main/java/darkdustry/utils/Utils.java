package darkdustry.utils;

import arc.files.Fi;
import arc.struct.OrderedMap;
import arc.util.Log;
import darkdustry.DarkdustryPlugin;
import mindustry.game.Team;
import mindustry.maps.MapException;
import mindustry.mod.Mods.LoadedMod;
import mindustry.net.WorldReloader;

import java.util.Locale;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.format;
import static darkdustry.components.Bundle.*;
import static java.time.Duration.ofMillis;
import static java.time.Instant.ofEpochMilli;
import static mindustry.Vars.*;

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
        return mods.getMod(DarkdustryPlugin.class);
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

    public static String formatHistoryDate(long time) {
        return historyFormat.format(ofEpochMilli(time));
    }

    public static String formatKickDate(long time) {
        return kickFormat.format(ofEpochMilli(time));
    }

    public static String formatDuration(long time) {
        return formatDuration(time, defaultLocale);
    }

    public static String formatDuration(long time, Locale locale) {
        var duration = ofMillis(time);
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
}