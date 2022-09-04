package darkdustry.components;

import arc.struct.StringMap;
import arc.util.*;
import darkdustry.DarkdustryPlugin;
import mindustry.game.Team;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.coloredTeam;
import static mindustry.Vars.content;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v" + mindustryVersion + "/core/assets/icons/icons.properties", response -> {
            for (String line : response.getResultAsString().split("\n")) {
                var values = line.split("\\|")[0].split("=");
                icons.put(values[1], String.valueOf((char) Integer.parseInt(values[0])));
            }

            for (var team : Team.baseTeams) {
                team.emoji = get(team.name, "");
            }

            // Временная мера, т.к. Анюк в будущем добавит эмодзи этим двум командам
            Team.green.emoji = get("electrified");
            Team.blue.emoji = get("wet");

            content.items().each(item -> items += " " + get(item.name) + item.name);
            content.units().each(unit -> !unit.internal, unit -> units += " " + get(unit.name) + unit.name);
            Structs.each(team -> teams += " " + coloredTeam(team), Team.baseTeams);

            DarkdustryPlugin.info("Loaded @ content icons.", icons.size);
        }, e -> DarkdustryPlugin.error("Unable to fetch content icons from GitHub. Check your internet connection."));
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
