package darkdustry.utils;

import arc.files.Fi;
import arc.func.Cons3;
import arc.struct.*;
import arc.util.CommandHandler.Command;
import arc.util.*;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.maps.*;
import mindustry.net.WorldReloader;
import mindustry.type.UnitType;
import mindustry.world.Block;
import useful.Bundle;

import java.time.*;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class Utils {

    // region common

    public static int voteChoice(String vote) {
        return switch (stripFooCharacters(vote.toLowerCase())) {
            case "y" -> 1;
            case "n" -> -1;
            default -> 0;
        };
    }

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    // endregion
    // region available

    public static Seq<Command> getAvailableCommands(Player player) {
        return netServer.clientCommands.getCommandList().select(command -> !hiddenCommands.contains(command.text) && (player.admin || !adminOnlyCommands.contains(command.text)));
    }

    public static Seq<Map> getAvailableMaps() {
        return maps.customMaps().isEmpty() ? maps.defaultMaps() : maps.customMaps();
    }

    public static Seq<Fi> getAvailableSaves() {
        return saveDirectory.seq().filter(fi -> fi.extEquals(saveExtension));
    }

    public static boolean isAvailable(UnitType type) {
        return type != null && !type.internal && !state.rules.isBanned(type) && type.supportsEnv(state.rules.env);
    }

    public static boolean isAvailable(Block block) {
        return block != null && block.inEditor && !state.rules.isBanned(block) && block.supportsEnv(state.rules.env);
    }

    // region strings

    public static String coloredTeam(Team team) {
        return "[#" + team.color + "]" + team.emoji + team.name + "[]";
    }

    public static String stripAll(String text) {
        return Strings.stripColors(Strings.stripGlyphs(text));
    }

    public static String stripDiscord(String text) {
        return stripFooCharacters(text).replace("`", "");
    }

    public static String stripFooCharacters(String text) {
        var builder = new StringBuilder(text);
        for (int i = text.length() - 1; i >= 0; i--)
            if (builder.charAt(i) >= 0xF80 && builder.charAt(i) <= 0x107F)
                builder.deleteCharAt(i);

        return builder.toString();
    }

    public static boolean deepEquals(String first, String second) {
        first = stripAll(first);
        second = stripAll(second);
        return first.equalsIgnoreCase(second) || first.toLowerCase().contains(second.toLowerCase());
    }

    public static String formatShortDate(long time) {
        return shortDateFormat.format(Instant.ofEpochMilli(time));
    }

    public static String formatLongDate(long time) {
        return longDateFormat.format(Instant.ofEpochMilli(time));
    }

    public static String formatDuration(long time, String locale) {
        var duration = Duration.ofMillis(time);
        var builder = new StringBuilder();

        OrderedMap.<String, Number>of(
                "time.days", duration.toDaysPart(),
                "time.hours", duration.toHoursPart(),
                "time.minutes", duration.toMinutesPart(),
                "time.seconds", duration.toSecondsPart()).each((key, value) -> {
            if (value.intValue() > 0)
                builder.append(" ").append(Bundle.format(key, locale, value));
        });

        return builder.substring(1);
    }

    public static <T> String formatList(Seq<T> content, int page, Cons3<StringBuilder, Integer, T> cons) {
        var builder = new StringBuilder();
        for (int i = maxPerPage * (page - 1); i < Math.min(maxPerPage * page, content.size); i++)
            cons.get(builder.append("\n\n"), i, content.get(i));

        return builder.substring(2);
    }

    // endregion
    // region world

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

    // endregion
}