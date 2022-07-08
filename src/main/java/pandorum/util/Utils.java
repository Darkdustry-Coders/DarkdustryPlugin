package pandorum.util;

import arc.Core;
import arc.files.Fi;
import arc.math.geom.Position;
import arc.struct.OrderedMap;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.server.ServerControl;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.components.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static mindustry.Vars.mods;
import static pandorum.PluginVars.*;

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
        return formatDuration(time, Bundle.defaultLocale());
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

        builder.setLength(Math.max(0, builder.length() - 1)); // Чтобы убрать пробел на конце

        return builder.toString();
    }

    public static Fi getPluginResource(String name) {
        return mods.locateMod("darkdustry-plugin").root.child(name);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }

    public static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }

    public static boolean isDangerousBuild(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    public static boolean isDangerousDeposit(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }
}
