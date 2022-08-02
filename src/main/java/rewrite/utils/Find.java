package rewrite.utils;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Locale;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Utils.*;

public class Find {

    public static Locale locale(String name) {
        return notNullElse(supportedLocales.find(locale -> name.equals(locale.toString()) || name.startsWith(locale.toString())), defaultLocale);
    }

    public static Player player(String name) {
        return canParsePositiveInt(name) ? Groups.player.getByID(parseInt(name)) : Groups.player.find(player -> deepEquals(player.name, name));
    }

    public static UnitType unit(String name) {
        return canParsePositiveInt(name) ? content.unit(parseInt(name)) : content.units().find(unit -> unit.name.equalsIgnoreCase(name));
    }

    public static Item item(String name) {
        return canParsePositiveInt(name) ? content.item(parseInt(name)) : content.items().find(item -> item.name.equalsIgnoreCase(name));
    }

    public static Team team(String name) {
        return canParsePositiveInt(name) ? Team.get(parseInt(name)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name));
    }

    public static Block block(String name) {
        return canParsePositiveInt(name) ? content.block(parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
    }

    public static Map map(String name) {
        Seq<Map> mapsList = maps.customMaps();
        return canParsePositiveInt(name) && parseInt(name) < mapsList.size ? mapsList.get(parseInt(name)) : mapsList.find(map -> deepEquals(map.name(), name));
    }

    public static Fi save(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        return canParsePositiveInt(name) && parseInt(name) < savesList.size ? savesList.get(parseInt(name)) : savesList.find(save -> deepEquals(save.nameWithoutExtension(), name));
    }

    public static Block core(String name) {
        return content.blocks().select(block -> block instanceof CoreBlock).find(block -> block.name.split("-")[1].equalsIgnoreCase(name));
    }
}
