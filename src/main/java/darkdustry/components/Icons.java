package darkdustry.components;

import arc.struct.StringMap;
import arc.util.Http;
import darkdustry.DarkdustryPlugin;
import mindustry.ctype.MappableContent;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.coloredTeam;
import static mindustry.Vars.content;
import static mindustry.game.Team.baseTeams;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/master/core/assets/icons/icons.properties", response -> {
            for (var line : response.getResultAsString().split("\n")) {
                var values = line.split("\\|")[0].split("=");
                icons.put(values[1], String.valueOf((char) Integer.parseInt(values[0])));
            }

            content.items().each(Icons::contains, item -> items.append(get(item)).append(item.name).append(" "));
            content.units().each(Icons::contains, unit -> items.append(get(unit)).append(unit.name).append(" "));

            for (var team : baseTeams) {
                team.emoji = get(team.name, "");
                teams.append(coloredTeam(team)).append(" ");
            }

            DarkdustryPlugin.info("Loaded @ content icons.", icons.size);
        }, e -> DarkdustryPlugin.error("Unable to fetch content icons from GitHub. Check your internet connection."));
    }

    public static String get(MappableContent content) {
        return get(content.name);
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }

    public static boolean contains(MappableContent content) {
        return icons.containsKey(content.name);
    }
}