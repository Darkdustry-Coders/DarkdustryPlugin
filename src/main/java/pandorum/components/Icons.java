package pandorum.components;

import arc.struct.StringMap;
import arc.util.Log;

import java.util.Scanner;

import static pandorum.util.Utils.getPluginResource;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        try (Scanner scanner = new Scanner(getPluginResource("icons.properties").read(512))) {
            while (scanner.hasNextLine()) {
                String[] lines = scanner.nextLine().split("=");
                String name = lines[1].split("\\|")[0];
                String icon = String.valueOf((char) Integer.parseInt(lines[0]));

                icons.put(name, icon);
            }

        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        return icons.get(key, defaultValue);
    }
}
