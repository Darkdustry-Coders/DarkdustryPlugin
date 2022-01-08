package pandorum;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.server.ServerControl;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;
import pandorum.struct.Tuple2;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static mindustry.Vars.*;
import static pandorum.PluginVars.config;

public class Misc {

    public static String colorizedTeam(Team team) {
        return Icons.get(team.name) + "[#" + team.color + "]" + team.name;
    }

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        for (int i = 0; i < mapsList.size; i++) {
            Map map = mapsList.get(i);
            if ((Strings.canParseInt(name) && i == Strings.parseInt(name) - 1) || Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name)) {
                return map;
            }
        }
        return null;
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(file -> file.extension().equals(saveExtension));
        for (int i = 0; i < savesList.size; i++) {
            Fi save = savesList.get(i);
            if ((Strings.canParseInt(name) && i == Strings.parseInt(name) - 1) || save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name)) {
                return save;
            }
        }
        return null;
    }

    public static Locale findLocale(String name) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> name.equals(l.toString()) || name.startsWith(l.toString()));
        return locale != null ? locale : Bundle.defaultLocale();
    }

    public static Player findPlayer(String name) {
        return Strings.canParseInt(name) ? Groups.player.getByID(Strings.parseInt(name)) : Groups.player.find(player -> Strings.stripGlyphs(Strings.stripColors(player.name)).equalsIgnoreCase(Strings.stripGlyphs(Strings.stripColors(name))) || Strings.stripGlyphs(Strings.stripColors(player.name)).contains(Strings.stripGlyphs(Strings.stripColors(name))));
    }

    public static Block findBlock(String name) {
        return Strings.canParseInt(name) ? content.block(Strings.parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
    }

    public static Block findCore(String name) {
        return switch (name.toLowerCase()) {
            case "small", "shard", "core-shard" -> Blocks.coreShard;
            case "medium", "foundation", "core-foundation" -> Blocks.coreFoundation;
            case "big", "nucleus", "core-nucleus" -> Blocks.coreNucleus;
            default -> null;
        };
    }

    public static UnitType findUnit(String name) {
        return Strings.canParseInt(name) ? content.unit(Strings.parseInt(name)) : content.units().find(unit -> unit.name.equalsIgnoreCase(name));
    }

    public static Item findItem(String name) {
        return Strings.canParseInt(name) ? content.item(Strings.parseInt(name)) : content.items().find(item -> item.name.equalsIgnoreCase(name));
    }

    public static Team findTeam(String name) {
        return Strings.canParseInt(name) ? Team.get(Strings.parseInt(name)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name));
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
        Groups.player.each(player -> bundled(player, key, values));
    }

    public static String formatTime(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        return format.format(time);
    }

    public static Tuple2<String, Integer> hubIp() {
        if (config.hubIp.contains(":")) {
            String[] parts = config.hubIp.split(":");
            return Tuple2.of(parts[0], Strings.parseInt(parts[1], port));
        }
        return Tuple2.of(config.hubIp, port);
    }

    public static ServerControl getServerControl() {
        return (ServerControl) Core.app.getListeners().find(listener -> listener instanceof ServerControl);
    }
}
