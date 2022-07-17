package pandorum.components;

import arc.struct.StringMap;
import arc.util.Http;
import arc.util.Log;
import mindustry.game.Team;

import java.util.Scanner;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v136/core/assets/icons/icons.properties").submit(response -> {
            try (Scanner scanner = new Scanner(response.getResultAsString())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().split("\\|")[0];
                    String[] lines = line.split("=");

                    String name = lines[1];
                    String icon = String.valueOf((char) Integer.parseInt(lines[0]));

                    icons.put(name, icon);
                }

                Team.derelict.emoji = get("derelict");
                Team.sharded.emoji = get("sharded");
                Team.crux.emoji = get("crux");
                Team.malis.emoji = get("malis");
                Team.green.emoji = get("shocked");
                Team.blue.emoji = get("spore-slowed");

                Log.info("[Darkdustry] Загружено иконок контента: @.", icons.size);
            }
        });
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
