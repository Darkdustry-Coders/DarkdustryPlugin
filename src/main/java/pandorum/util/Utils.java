package pandorum.util;

import arc.Core;
import arc.files.Fi;
import arc.struct.OrderedMap;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.server.ServerControl;
import pandorum.components.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static mindustry.Vars.mods;

public class Utils {

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static int voteChoice(String value) {
        return switch (value.toLowerCase()) {
            case "y", "yes", "+" -> 1;
            case "n", "no", "-" -> -1;
            default -> 0;
        };
    }

    public static boolean deepEquals(String first, String second) {
        return stripAll(first).equalsIgnoreCase(stripAll(second)) || stripAll(first).toLowerCase().contains(stripAll(second).toLowerCase());
    }

    public static String stripAll(String str) {
        return Strings.stripColors(Strings.stripGlyphs(str));
    }

    public static String coloredTeam(Team team) {
        return team.emoji + "[#" + team.color + "]" + team.name;
    }

    public static String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(new Date(time));
    }

    public static String formatDuration(long time) {
        return formatDuration(time, Bundle.defaultLocale);
    }

    public static String formatDuration(long time, Locale locale) {
        Duration duration = Duration.ofMillis(time);
        StringBuilder builder = new StringBuilder();
        OrderedMap.<String, Integer>of(
                "time.days", (int) duration.toDaysPart(),
                "time.hours", duration.toHoursPart(),
                "time.minutes", duration.toMinutesPart(),
                "time.seconds", duration.toSecondsPart()
        ).each((key, value) -> {
            if (value > 0) builder.append(Bundle.format(key, locale, value)).append(" ");
        });

        return builder.toString().trim();
    }

    public static Fi getPluginResource(String name) {
        return mods.getMod("darkdustry-plugin").root.child(name);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }
}
