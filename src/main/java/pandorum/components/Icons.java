package pandorum.components;

import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Log;
import mindustry.game.Team;

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

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
