package rewrite.components;

import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Structs;
import mindustry.game.Team;
import rewrite.DarkdustryPlugin;

import static mindustry.Vars.content;
import static rewrite.PluginVars.*;
import static rewrite.utils.Utils.coloredTeam;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v136.1/core/assets/icons/icons.properties").submit(response -> {
            for (String line : response.getResultAsString().split("\n")) {
                String[] values = line.split("\\|")[0].split("=");
                icons.put(values[1], String.valueOf((char) Integer.parseInt(values[0])));
            }

            Team.derelict.emoji = get("derelict");
            Team.sharded.emoji = get("sharded");
            Team.crux.emoji = get("crux");
            Team.malis.emoji = get("malis");
            Team.green.emoji = get("electrified");
            Team.blue.emoji = get("wet");

            content.items().each(item -> items += " " + get(item.name) + item.name);
            content.units().each(unit -> units += " " + get(unit.name) + unit.name);
            Structs.each(team -> teams += " " + coloredTeam(team), Team.baseTeams);

            DarkdustryPlugin.info("Загружено @ иконок контента.", icons.size);
        });
    }

    public static String get(String key) {
        return get(key, key);
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
