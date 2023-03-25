package darkdustry.utils;

import arc.files.Fi;
import arc.func.Boolf;
import arc.struct.Seq;
import arc.util.Structs;
import darkdustry.features.Ranks.Rank;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.util.Strings.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class Find {

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

    public static Team team(String name) {
        return canParsePositiveInt(name) ? Team.get(parseInt(name)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(name));
    }

    public static UnitType unit(String name) {
        return findContent(name, ContentType.unit);
    }

    public static Block block(String name) {
        return findContent(name, ContentType.block);
    }

    public static Item item(String name) {
        return findContent(name, ContentType.item);
    }

    public static StatusEffect effect(String name) {
        return findContent(name, ContentType.status);
    }

    public static Block core(String name) {
        var block = block(name);
        return block instanceof CoreBlock ? block : null;
    }

    public static Map map(String name) {
        return findInSeq(name, getAvailableMaps(), map -> deepEquals(map.name(), name));
    }

    public static Fi save(String name) {
        return findInSeq(name, getAvailableSaves(), save -> deepEquals(save.nameWithoutExtension(), name));
    }

    public static Gamemode mode(String name) {
        return findInEnum(name, Gamemode.values(), mode -> mode.name().equalsIgnoreCase(name));
    }

    public static Rank rank(String name) {
        return findInEnum(name, Rank.values(), rank -> rank.name().equalsIgnoreCase(name));
    }

    // region utils

    public static <T extends UnlockableContent> T findContent(String name, ContentType type) {
        return canParsePositiveInt(name) ? content.getByID(type, parseInt(name)) : content.getByName(type, name);
    }

    public static <T> T findInSeq(String name, Seq<T> values, Boolf<T> filter) {
        int index = parseInt(name) - 1;
        if (index > 0 && index < values.size)
            return values.get(index);

        return values.find(filter);
    }

    public static <T extends Enum<T>> T findInEnum(String name, T[] values, Boolf<T> filter) {
        int index = parseInt(name) - 1;
        if (index > 0 && index < values.length)
            return values[index];

        return Structs.find(values, filter);
    }

    // endregion
}