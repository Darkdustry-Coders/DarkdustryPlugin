package pandorum.components;

import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;

import java.util.Scanner;

import static mindustry.Vars.content;
import static pandorum.util.Utils.coloredTeam;
import static pandorum.util.Utils.getPluginFile;

public class Icons {

    private static final StringMap icons = new StringMap();

    public static void load() {
        try (Scanner scanner = new Scanner(getPluginFile().child("block_colors.png").read(512))) {
            while (scanner.hasNextLine()) {
                String[] split = scanner.nextLine().split("=");

                String name = split[1].split("\\|")[0];
                String character = (char) Strings.parseInt(split[0]) + "";

                icons.put(name, character);
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

    public static String unitsList() {
        StringBuilder units = new StringBuilder();
        content.units().each(unit -> units.append(" [white]").append(get(unit.name)).append(unit.name));
        return units.toString();
    }

    public static String itemsList() {
        StringBuilder items = new StringBuilder();
        content.items().each(item -> items.append(" [white]").append(get(item.name)).append(item.name));
        return items.toString();
    }

    public static String teamsList() {
        StringBuilder teams = new StringBuilder();
        Structs.each(team -> teams.append(" [white]").append(coloredTeam(team)), Team.baseTeams);
        return teams.toString();
    }
}
