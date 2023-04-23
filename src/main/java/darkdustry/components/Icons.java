package darkdustry.components;

import arc.struct.*;
import arc.util.*;
import darkdustry.DarkdustryPlugin;
import mindustry.ctype.MappableContent;
import mindustry.game.Team;
import mindustry.gen.Iconc;

import static darkdustry.utils.Utils.*;

public class Icons {

    public static final StringMap icons = new StringMap();
    public static final Seq<Character> sides = Seq.with(Iconc.right, Iconc.up, Iconc.left, Iconc.down);

    public static void load() {
        Http.get("https://raw.githubusercontent.com/Anuken/Mindustry/master/core/assets/icons/icons.properties", response -> {
            for (var line : response.getResultAsString().split("\n")) {
                var values = line.split("\\|")[0].split("=");
                icons.put(values[1], String.valueOf((char) Integer.parseInt(values[0])));
            }

            Structs.each(team -> team.emoji = icons.get(team.name, ""), Team.baseTeams);

            DarkdustryPlugin.info("Loaded @ content icons.", icons.size);
        }, e -> DarkdustryPlugin.error("Unable to fetch content icons from GitHub. Check your internet connection."));
    }

    public static String teamsList() {
        var builder = new StringBuilder();
        Structs.each(team -> builder.append(coloredTeam(team)).append(" "), Team.baseTeams);

        return builder.toString();
    }

    public static String contentList(Seq<? extends MappableContent> contents) {
        var builder = new StringBuilder();
        contents.each(content -> builder.append(name(content)).append(" "));

        return builder.toString();
    }

    public static String icon(MappableContent content) {
        return icons.get(content.name, "");
    }

    public static String name(MappableContent content) {
        return icon(content) + content.name;
    }
}