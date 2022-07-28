package pandorum.util;

import arc.util.Strings;
import mindustry.game.Team;

public class StringUtils {

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
}
