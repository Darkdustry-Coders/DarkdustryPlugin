package pandorum.components;

import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Log;
import arc.util.Structs;
import mindustry.game.Team;

import static mindustry.Vars.content;
import static pandorum.PluginVars.*;
import static pandorum.util.StringUtils.coloredTeam;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v136/core/assets/icons/icons.properties").submit(response -> {
            String[] lines = response.getResultAsString().split("\n");
            for (String line : lines) {
                String[] values = line.split("\\|")[0].split("=");

                String name = values[1];
                String icon = String.valueOf((char) Integer.parseInt(values[0]));

                icons.put(name, icon);
            }

            Team.derelict.emoji = get("derelict");
            Team.sharded.emoji = get("sharded");
            Team.crux.emoji = get("crux");
            Team.malis.emoji = get("malis");
            Team.green.emoji = get("shocked");
            Team.blue.emoji = get("wet");

            Log.info("[Darkdustry] Загружено иконок контента: @.", icons.size);
        });
    }

    // TODO упростить
    public static void loadLists() {
        content.items().each(item -> itemsList += " [white]" + get(item.name) + item.name);

        content.units().each(unit -> unitsList += " [white]" + get(unit.name) + unit.name);

        Structs.each(team -> teamsList += " [white]" + coloredTeam(team), Team.baseTeams);
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
