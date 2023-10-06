package darkdustry.utils;

import arc.files.Fi;
import arc.func.*;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.type.UnitType;
import mindustry.world.Block;

import java.time.Duration;

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

    public static Seq<Command> availableCommands(Player player) {
        return netServer.clientCommands.getCommandList().select(command -> !hiddenCommands.contains(command.text) && (player.admin || !adminOnlyCommands.contains(command.text)));
    }

    public static Seq<Map> availableMaps() {
        return maps.customMaps().any() ? maps.customMaps() : maps.defaultMaps();
    }

    public static Seq<Fi> availableSaves() {
        return Seq.select(saveDirectory.list(), fi -> fi.extEquals(saveExtension));
    }

    public static boolean available(UnitType type) {
        return !type.internal && !state.rules.isBanned(type) && type.supportsEnv(state.rules.env);
    }

    public static boolean available(Block block) {
        return block.inEditor && !state.rules.isBanned(block) && block.supportsEnv(state.rules.env);
    }

    // region strings

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
        return first.toLowerCase().contains(second.toLowerCase());
    }

    public static Duration parseDuration(String input) {
        var matcher = durationPattern.matcher(input);
        long seconds = 0L;

        while (matcher.find()) {
            long amount = Strings.parseLong(matcher.group(1), 0);
            if (amount <= 0) continue;

            for (var tuple : durationPatterns) {
                if (tuple.getT1().matcher(matcher.group(2).toLowerCase()).matches()) {
                    seconds += tuple.getT2().getDuration().multipliedBy(amount).getSeconds();
                    break;
                }
            }
        }

        return Duration.ofSeconds(seconds);
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

    public static <T> String formatList(Seq<T> values, int page, Cons3<StringBuilder, Integer, T> formatter) {
        var builder = new StringBuilder();

        for (int i = maxPerPage * (page - 1); i < Math.min(maxPerPage * page, values.size); i++) {
            if (!builder.isEmpty()) builder.append("\n");
            formatter.get(builder, i + 1, values.get(i));
        }

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
}