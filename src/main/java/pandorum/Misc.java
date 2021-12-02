package pandorum;

import arc.files.Fi;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static mindustry.Vars.maps;
import static mindustry.Vars.saveExtension;
import static mindustry.Vars.saveDirectory;

public abstract class Misc {

    private Misc() {}

    public static String colorizedTeam(Team team) {
        return Strings.format("@[#@]@", Icons.get(team.name), team.color, team.name);
    }

    public static Map findMap(String name) {
        for (int i = 0; i < maps.customMaps().size; i++) {
            Map map = maps.customMaps().get(i);
            if ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name)) {
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String name) {
        for (int i = 0; i < saveDirectory.list().length; i++) {
            Fi save = saveDirectory.list()[i];
            if (Objects.equals(save.extension(), saveExtension) && ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name))) {
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

    public static String formatTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(time);
    }
}
