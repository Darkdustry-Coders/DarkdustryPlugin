package darkdustry.components;

import arc.struct.*;
import arc.util.*;
import darkdustry.DarkdustryPlugin;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;

import static darkdustry.utils.Utils.*;

public class Icons {

    public static final StringMap icons = new StringMap();

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/master/core/assets/icons/icons.properties", response -> {
            for (var line : response.getResultAsString().split("\n")) {
                var values = line.split("\\|")[0].split("=");
                icons.put(values[1], String.valueOf((char) Integer.parseInt(values[0])));
            }

            for (var team : Team.baseTeams)
                team.emoji = icons.get(team.name, "");

            DarkdustryPlugin.info("Loaded @ content icons.", icons.size);
        }, e -> DarkdustryPlugin.error("Unable to fetch content icons from GitHub. Check your internet connection."));
    }

    public static String teamsList() {
        var builder = new StringBuilder();
        Structs.each(team -> builder.append(coloredTeam(team)).append(" "), Team.baseTeams);

        return builder.toString();
    }

    public static String contentList(Seq<? extends UnlockableContent> contents) {
        var builder = new StringBuilder();
        contents.each(content -> builder.append(icon(content)).append(content.name).append(" "));

        return builder.toString();
    }

    public static String icon(UnlockableContent content) {
        if (false)
            return content.emoji();

        return icons.get(content.name, "");
    }
}