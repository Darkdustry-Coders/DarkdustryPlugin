package pandorum;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static mindustry.Vars.*;

public abstract class Misc {

    private Misc() {
    }

    public static String colorizedTeam(Team team) {
        return "[white]" + Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        for (int i = 0; i < mapsList.size; i++) {
            Map map = mapsList.get(i);
            if ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name)) {
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(f -> Objects.equals(f.extension(), saveExtension));
        for (int i = 0; i < savesList.size; i++) {
            Fi save = savesList.get(i);
            if ((Strings.canParsePositiveInt(name) && i == Strings.parseInt(name) - 1) || save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name)) {
                return save;
            }
        }
        return null;
    }

    public static Locale findLocale(String language) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> l.toString().equals(language) || language.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
    }

    public static Player findByName(String name) {
        return Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(Strings.stripColors(name)) || Strings.stripColors(p.name).contains(Strings.stripColors(name)));
    }

    public static Block findBlock(String name) {
        return Strings.canParseInt(name) ? content.block(Strings.parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
    }

    public static UnitType findUnit(String name) {
        return Strings.canParseInt(name) ? content.unit(Strings.parseInt(name)) : content.units().find(unit -> unit.name.equalsIgnoreCase(name));
    }

    public static Item findItem(String name) {
        return Strings.canParseInt(name) ? content.item(Strings.parseInt(name)) : content.items().find(item -> item.name.equalsIgnoreCase(name));
    }

    public static Team findTeam(String name) {
        return Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name) || (Strings.canParseInt(name) && team.id == Strings.parseInt(name)));
    }

    public static boolean adminCheck(Player player) {
        if (!player.admin) {
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
