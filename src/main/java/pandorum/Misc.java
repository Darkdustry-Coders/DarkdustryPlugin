package pandorum;

import arc.files.Fi;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.net.Packets.KickReason;
import pandorum.comp.Bundle;
import pandorum.struct.Tuple2;

import java.util.Locale;

import static mindustry.Vars.maps;
import static mindustry.Vars.saveDirectory;

public abstract class Misc {

    private Misc() {}

    public static String colorizedTeam(Team team) {
        return Strings.format("[#@]@", team.color, team);
    }

    public static Map findMap(String text) {
        for (int i = 0; i < maps.customMaps().size; i++) {
            Map map = maps.customMaps().get(i);
            if ((Strings.canParseInt(text) && i == Strings.parseInt(text) - 1) || Strings.stripColors(map.name()).equalsIgnoreCase(text) || Strings.stripColors(map.name()).contains(text)) {
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String text) {
        for (int i = 0; i < saveDirectory.list().length; i++) {
            Fi save = saveDirectory.list()[i];
            if ((Strings.canParseInt(text) && i == Strings.parseInt(text) - 1) || save.nameWithoutExtension().equalsIgnoreCase(text) || save.nameWithoutExtension().contains(text)) {
                return save;
            }
        }
        return null;
    }

    public static Player findByName(String name) {
        return Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(Strings.stripColors(name)) || Strings.stripColors(p.name).contains(Strings.stripColors(name)));
    }

    public static Player findByID(String uuid) {
        return Groups.player.find(p -> p.uuid().equals(uuid));
    }

    public static Locale findLocale(String lang) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(lang) || lang.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
    }

    /**
     * @param player A player to check
     * @return true if a player isn't an admin, false if he is
     */
    public static boolean adminCheck(Player player) {
        if (!player.admin()) {
            bundled(player, "commands.permission-denied");
            return true;
        }
        return false;
    }

    public static void bundled(Player player, String key, Object... values) {
        player.sendMessage(Bundle.format(key, findLocale(player.locale), values));
    }

    public static void sendToChat(String key, Object... values) {
        Groups.player.each(p -> bundled(p, key, values));
    }

    public static boolean nameCheck(Player player) {
        String name = Strings.stripColors(player.name);
        if (name.length() < 1 || name.length() > 30) {
            player.con.kick(Bundle.format("events.bad-name-length", findLocale(player.locale)), 0);
            return true;
        }
        if (name.contains("@")) {
            player.con.kick(KickReason.kick, 0);
            return true;
        }
        return false;
    }

    public static void connectToHub(Player player) {
        Tuple2<String, Integer> hub = PandorumPlugin.config.getIp();
        Vars.net.pingHost(hub.t1, hub.t2, host -> Call.connect(player.con, hub.t1, hub.t2), e -> bundled(player, "commands.hub.offline"));
    }
}
