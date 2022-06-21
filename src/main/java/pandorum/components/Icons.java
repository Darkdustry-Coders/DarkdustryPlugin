package pandorum.components;

import arc.files.Fi;
import arc.struct.StringMap;
import arc.util.Log;

import java.util.Scanner;

import static pandorum.util.Utils.getPluginResource;

public class Icons {

    private static final StringMap stringIcons = new StringMap();

    public static void load() {
        Fi icons = getPluginResource("icons.properties");

        try (Scanner scanner = new Scanner(icons.read(512))) {
            while (scanner.hasNextLine()) {
                String[] lines = scanner.nextLine().split("="), names = lines[1].split("\\|");
                String name = names[0], icon = String.valueOf((char) Integer.parseInt(lines[0]));

                stringIcons.put(name, icon);
            }
        } catch (Exception e) {
            Log.err("[Darkdustry] Файл 'icons.properties' не найден или повреждён", e);
        }
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return stringIcons.get(key, defaultValue);
    }
}
