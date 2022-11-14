package darkdustry.utils;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.*;
import darkdustry.DarkdustryPlugin;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.*;
import mindustry.net.WorldReloader;

import static darkdustry.PluginVars.*;
import static java.time.Instant.ofEpochMilli;
import static mindustry.Vars.*;

public class Utils {

    public static int voteChoice(String vote) {
        return switch (stripAll(vote.toLowerCase())) {
            case "y" -> 1;
            case "n" -> -1;
            default -> 0;
        };
    }

    public static <T> T notNullElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static Fi getPluginResource(String name) {
        return mods.getMod(DarkdustryPlugin.class).root.child(name);
    }

    public static Seq<Command> getAvailableCommands(Player player) {
        return netServer.clientCommands.getCommandList().select(command -> !hiddenCommands.contains(command.text) && (player.admin || !adminOnlyCommands.contains(command.text)));
    }

    public static Seq<Map> getAvailableMaps() {
        return maps.customMaps().isEmpty() ? maps.defaultMaps() : maps.customMaps();
    }

    public static Seq<Fi> getAvailableSaves() {
        return saveDirectory.seq().filter(SaveIO::isSaveValid);
    }

    public static String coloredTeam(Team team) {
        return "[#" + team.color + "]" + team.emoji + team.name + "[]";
    }

    public static String stripAll(String str) {
        return Strings.stripColors(Strings.stripGlyphs(str));
    }

    public static boolean deepEquals(String first, String second) {
        first = stripAll(first);
        second = stripAll(second);
        return first.equalsIgnoreCase(second) || first.toLowerCase().contains(second.toLowerCase());
    }

    public static String formatHistoryDate(long time) {
        return historyFormat.format(ofEpochMilli(time));
    }

    public static String formatKickDate(long time) {
        return kickFormat.format(ofEpochMilli(time));
    }

    public static void reloadWorld(Runnable load) {
        try {
            var reloader = new WorldReloader();
            reloader.begin();

            load.run();
            state.rules = state.map.applyRules(state.rules.mode());
            logic.play();

            reloader.end();
        } catch (MapException e) {
            Log.err("@: @", e.map.name(), e.getMessage());
            net.closeServer();
        }
    }
}