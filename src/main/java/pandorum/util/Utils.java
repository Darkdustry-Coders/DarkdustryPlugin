package pandorum.util;

import arc.Core;
import arc.files.Fi;
import arc.struct.OrderedMap;
import mindustry.server.ServerControl;
import pandorum.components.Bundle;
import pandorum.features.history.HistorySeq;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static mindustry.Vars.mods;
import static pandorum.PluginVars.history;
import static pandorum.PluginVars.maxTileHistoryCapacity;

public class Utils {

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
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
        return mods.list().find(mod -> mod.name.equals("darkdustry-plugin")).root.child(name);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }

    public static HistorySeq getHistory(int x, int y) {
        HistorySeq entries = history[x][y];
        if (entries == null) {
            history[x][y] = entries = new HistorySeq(maxTileHistoryCapacity);
        }

        return entries;
    }
}
