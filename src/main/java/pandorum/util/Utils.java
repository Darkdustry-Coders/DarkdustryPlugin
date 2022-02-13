package pandorum.util;

import arc.Core;
import arc.func.Cons;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.server.ServerControl;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static int voteChoise(String value) {
        return switch (value.toLowerCase()) {
            case "y", "yes", "д", "да", "так", "+" -> 1;
            case "n", "no", "н", "нет", "ні", "-" -> -1;
            default -> 0;
        };
    }

    public static String stripAll(String str) {
        return Strings.stripColors(Strings.stripGlyphs(str));
    }

    public static String colorizedTeam(Team team) {
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static void eachPlayerInTeam(Team team, Cons<Player> cons) {
        Groups.player.each(player -> player.team() == team, cons);
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

    public static String formatDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(new Date(time));
    }

    public static String formatDuration(long time) {
        Duration duration = Duration.ofMillis(time);
        StringBuilder builder = new StringBuilder();
        if (duration.toDaysPart() > 0) builder.append(duration.toDaysPart()).append(" d ");
        if (duration.toHoursPart() > 0) builder.append(duration.toHoursPart()).append(" h ");
        if (duration.toMinutesPart() > 0) builder.append(duration.toMinutesPart()).append(" min ");

        builder.append(duration.toSecondsPart()).append(" sec");
        return builder.toString();
    }

    public static long millisecondsToMinutes(long time) {
        return TimeUnit.MILLISECONDS.toMinutes(time);
    }

    public static long secondsToMinutes(long time) {
        return TimeUnit.SECONDS.toMinutes(time);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }
}
