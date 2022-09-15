package darkdustry.utils;

import arc.files.Fi;
import arc.util.Structs;
import darkdustry.features.Ranks.Rank;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Locale;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.translatorLanguages;
import static darkdustry.components.Bundle.*;
import static darkdustry.features.Ranks.Rank.ranks;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class Find {

    public static Locale locale(String name) {
        return notNullElse(supportedLocales.find(locale -> name.startsWith(locale.toString())), defaultLocale);
    }

    public static Player player(String nameOrId) {
        return notNullElse(playerById(nameOrId), playerByName(nameOrId));
    }

    public static Player playerById(String id) {
        return id.startsWith("#") ? Groups.player.getByID(parseInt(id.substring(1))) : null;
    }

    public static Player playerByName(String name) {
        return Groups.player.find(player -> deepEquals(player.name, name));
    }

    public static Player playerByUuid(String uuid) {
        return Groups.player.find(player -> player.uuid().equals(uuid));
    }

    public static PlayerInfo playerInfo(String name) {
        var player = player(name);
        if (player != null) return player.getInfo();

        return notNullElse(netServer.admins.getInfoOptional(name), netServer.admins.findByIP(name));
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

    public static Rank rank(String name) {
        return canParsePositiveInt(name) ? ranks.get(parseInt(name)) : ranks.find(rank -> rank.name.equalsIgnoreCase(name));
    }

    public static Map map(String name) {
        var list = maps.customMaps();
        int index = parseInt(name) - 1;
        return index >= 0 && index < list.size ? list.get(index) : list.find(map -> deepEquals(map.name(), name));
    }

    public static Fi save(String name) {
        var list = saveDirectory.seq().filter(SaveIO::isSaveValid);
        int index = parseInt(name) - 1;
        return index >= 0 && index < list.size ? list.get(index) : list.find(save -> deepEquals(save.nameWithoutExtension(), name));
    }

    public static Gamemode mode(String name) {
        return Structs.find(Gamemode.all, mode -> mode.name().equalsIgnoreCase(name));
    }

    public static Block core(String name) {
        return content.blocks().select(CoreBlock.class::isInstance).find(block -> block.name.split("-")[1].equalsIgnoreCase(name));
    }

    public static String language(String name) {
        return translatorLanguages.orderedKeys().find(name::startsWith);
    }
}
