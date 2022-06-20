package pandorum.util;

import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Unit;
import pandorum.components.Icons;

import static mindustry.Vars.content;

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
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static String unitsList() {
        StringBuilder units = new StringBuilder();
        content.units().each(unit -> units.append(" [white]").append(Icons.get(unit.name)).append(unit.name));
        return units.toString();
    }

    public static String itemsList() {
        StringBuilder items = new StringBuilder();
        content.items().each(item -> items.append(" [white]").append(Icons.get(item.name)).append(item.name));
        return items.toString();
    }

    public static String teamsList() {
        StringBuilder teams = new StringBuilder();
        for (Team team : Team.baseTeams) teams.append(" [white]").append(coloredTeam(team));
        return teams.toString();
    }

    public static String getUnitName(Unit unit) {
        return Utils.notNullElse(unit.getControllerName(), Icons.get(unit.type.name));
    }
}
