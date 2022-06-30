package pandorum.components;

import arc.struct.StringMap;
import arc.util.Http;

import java.util.Scanner;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/v135/core/assets/icons/icons.properties").submit(response -> {
            try (Scanner scanner = new Scanner(response.getResultAsString())) {
                while (scanner.hasNextLine()) {
                    String[] lines = scanner.nextLine().split("="), names = lines[1].split("\\|");
                    String name = names[0], icon = String.valueOf((char) Integer.parseInt(lines[0]));

                    icons.put(name, icon);
                }
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
