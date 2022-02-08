package pandorum.util;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.components.Bundle;

import java.util.Locale;

import static mindustry.Vars.*;

public class Search {

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        return Strings.canParsePositiveInt(name) ? mapsList.get(Strings.parseInt(name) - 1) : mapsList.find(map -> Strings.stripColors(map.name()).equalsIgnoreCase(name) || Strings.stripColors(map.name()).contains(name));
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        return Strings.canParsePositiveInt(name) ? savesList.get(Strings.parseInt(name) - 1) : savesList.find(save -> save.nameWithoutExtension().equalsIgnoreCase(name) || save.nameWithoutExtension().contains(name));
    }

    public static Locale findLocale(String name) {
        Locale locale = Structs.find(Bundle.supportedLocales, l -> name.equals(l.toString()) || name.startsWith(l.toString()));
        return Utils.notNullElse(locale, Bundle.defaultLocale());
    }

    public static Player findPlayer(String name) {
        return Strings.canParsePositiveInt(name) ? Groups.player.getByID(Strings.parseInt(name)) : Groups.player.find(player -> Utils.stripAll(player.name).equalsIgnoreCase(Utils.stripAll(name)));
    }

    public static Block findBlock(String name) {
        return Strings.canParsePositiveInt(name) ? content.block(Strings.parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
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
        return Strings.canParsePositiveInt(name) ? content.unit(Strings.parseInt(name)) : content.units().find(unit -> unit.name.equalsIgnoreCase(name));
    }

    public static Item findItem(String name) {
        return Strings.canParsePositiveInt(name) ? content.item(Strings.parseInt(name)) : content.items().find(item -> item.name.equalsIgnoreCase(name));
    }

    public static Team findTeam(String name) {
        return Strings.canParsePositiveInt(name) ? Team.get(Strings.parseInt(name)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name));
    }
}
