package rewrite.utils;

import arc.files.Fi;
import mindustry.game.Team;

import static arc.util.Strings.*;
import static mindustry.Vars.*;


public class Utils {

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static Fi getPluginResource(String name) {
        return mods.getMod("darkdustry-plugin").root.child(name);
    }

    public static String coloredTeam(Team team) {
        return team.emoji + "[#" + team.color + "]" + team.name + "[]";
    }

    public static boolean deepEquals(String first, String second) {
        first = stripAll(first);
        second = stripAll(second);
        return first.equalsIgnoreCase(second) || first.toLowerCase().contains(second.toLowerCase());
    }

    public static String stripAll(CharSequence str) {
        return stripColors(stripGlyphs(str));
    }

}
