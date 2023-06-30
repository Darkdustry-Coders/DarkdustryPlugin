package darkdustry.utils;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.maps.*;
import mindustry.net.WorldReloader;
import mindustry.type.UnitType;
import mindustry.world.Block;

import java.text.SimpleDateFormat;
import java.time.*;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static discord4j.common.util.TimestampFormat.*;
import static mindustry.Vars.*;

public class Utils {

    // region common

    public static int voteChoice(String vote) {
        return switch (stripFooCharacters(vote.toLowerCase())) {
            case "y", "yes" -> 1;
            case "n", "no" -> -1;
            default -> 0;
        };
    }

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    // endregion
    // region available

    public static Seq<Command> availableCommands(Player player) {
        return netServer.clientCommands.getCommandList().select(command -> !hiddenCommands.contains(command.text) && (player.admin || !adminOnlyCommands.contains(command.text)));
    }

    public static Seq<Map> availableMaps() {
        return maps.customMaps().any() ? maps.customMaps() : maps.defaultMaps();
    }

    public static Seq<Fi> availableSaves() {
        return saveDirectory.seq().filter(fi -> fi.extEquals(saveExtension));
    }

    public static boolean available(UnitType type) {
        return !type.internal && !state.rules.isBanned(type) && type.supportsEnv(state.rules.env);
    }

    public static boolean available(Block block) {
        return block.inEditor && !state.rules.isBanned(block) && block.supportsEnv(state.rules.env);
    }

    // region strings

    public static String stripAll(String text) {
        return stripColors(stripGlyphs(text));
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
        return first.toLowerCase().contains(second.toLowerCase());
    }

    // endregion
    // region time

    public static String formatTime(long time) {
        return SimpleDateFormat.getTimeInstance().format(time);
    }

    public static String formatDateTime(long time) {
        return SimpleDateFormat.getDateTimeInstance().format(time);
    }

    public static String formatTimestamp(long time) {
        return LONG_DATE.format(Instant.ofEpochMilli(time));
    }

    public static Duration parseDuration(String input) {
        var matcher = durationPattern.matcher(input);
        long time = 0L;

        while (matcher.find()) {
            long amount = Strings.parseLong(matcher.group(1), 0);
            if (amount <= 0) continue;

            for (var tuple : durationPatterns) {
                if (tuple.getT1().matcher(matcher.group(2).toLowerCase()).matches()) {
                    time += tuple.getT2().toSeconds(amount);
                    break;
                }
            }
        }

        return Duration.ofSeconds(time);
    }

    // endregion
    // region format

    public static String formatTeams() {
        var builder = new StringBuilder();
        Structs.each(team -> builder.append(team.coloredName()).append(" "), Team.baseTeams);

        return builder.toString();
    }

    public static String formatContents(Seq<? extends UnlockableContent> contents) {
        var builder = new StringBuilder();
        contents.each(content -> builder.append(content.emoji()).append(content.name).append(" "));

        return builder.toString();
    }

    public static String formatRotation(int rotation) {
        return switch (rotation) {
            case 0 -> "\uE803";
            case 1 -> "\uE804";
            case 2 -> "\uE802";
            case 3 -> "\uE805";
            default -> "⚠";
        };
    }

    // endregion
    // region world

    public static void reloadWorld(Runnable runnable) {
        try {
            var reloader = new WorldReloader();
            reloader.begin();

            runnable.run();
            state.rules = state.map.applyRules(state.rules.mode());

            logic.play();
            reloader.end();
        } catch (MapException e) {
            Log.err("@: @", e.map.name(), e.getMessage());
        }
    }

    // endregion
}