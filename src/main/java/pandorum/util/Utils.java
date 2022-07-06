package pandorum.util;

import arc.Core;
import arc.files.Fi;
import arc.math.geom.Position;
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
        if (duration.toDaysPart() > 0) builder.append(duration.toDaysPart()).append(Bundle.get("time.days", locale));
        if (duration.toHoursPart() > 0) builder.append(duration.toHoursPart()).append(Bundle.get("time.hours", locale));
        if (duration.toMinutesPart() > 0) builder.append(duration.toMinutesPart()).append(Bundle.get("time.minutes", locale));
        if (duration.toSecondsPart() > 0) builder.append(duration.toSecondsPart()).append(Bundle.get("time.seconds", locale));

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
