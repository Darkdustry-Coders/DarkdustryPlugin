package darkdustry.utils;

import arc.files.Fi;
import arc.func.Boolf;
import arc.struct.Seq;
import arc.util.*;
import darkdustry.database.*;
import darkdustry.database.models.PlayerData;
import darkdustry.features.Ranks.Rank;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Optional;

import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class Find {

    public static Player player(String input) {
        if (canParseID(input)) {
            var player = Groups.player.getByID(parseID(input));
            if (player != null) return player;

            var data = Cache.get(parseID(input));
            if (data != null) return playerByUUID(data.uuid);
        }

        return Groups.player.find(player -> deepEquals(player.name, input));
    }

    public static Player playerByUUID(String input) {
        return Groups.player.find(player -> player.uuid().equals(input));
    }

    public static PlayerInfo playerInfo(String input) {
        var player = player(input);
        if (player != null) return player.getInfo();

        if (canParseID(input)) {
            var data = Database.getPlayerData(parseID(input));
            if (data != null) return netServer.admins.getInfoOptional(data.uuid);
        }

        return Optional.ofNullable(netServer.admins.getInfoOptional(input)).orElseGet(() -> netServer.admins.findByIP(input));
    }

    public static PlayerData playerData(String input) {
        var player = player(input);
        if (player != null) return Cache.get(player);

        return canParseID(input) ? Database.getPlayerData(parseID(input)) : Database.getPlayerData(input);
    }

    public static Team team(String input) {
        return canParseID(input) ? Team.get(parseID(input)) : Structs.find(Team.all, team -> team.name.equalsIgnoreCase(input));
    }

    public static UnitType unit(String input) {
        return findContent(input, ContentType.unit);
    }

    public static Block block(String input) {
        return findContent(input, ContentType.block);
    }

    public static Item item(String input) {
        return findContent(input, ContentType.item);
    }

    public static StatusEffect effect(String input) {
        return findContent(input, ContentType.status);
    }

    public static Block core(String input) {
        var block = block(input);
        return block instanceof CoreBlock ? block : null;
    }

    public static Map map(String input) {
        return findSeq(input, availableMaps(), map -> deepEquals(map.name(), input));
    }

    public static Fi save(String input) {
        return findSeq(input, availableSaves(), save -> deepEquals(save.nameWithoutExtension(), input));
    }

    public static Gamemode mode(String input) {
        return findArray(input, Gamemode.values(), mode -> mode.name().equalsIgnoreCase(input));
    }

    public static Rank rank(String input) {
        return findArray(input, Rank.values(), rank -> rank.name().equalsIgnoreCase(input));
    }

    // region utils

    private static <T extends UnlockableContent> T findContent(String input, ContentType type) {
        return canParseID(input) ? content.getByID(type, parseID(input)) : content.getByName(type, input);
    }

    private static <T> T findSeq(String input, Seq<T> values, Boolf<T> filter) {
        int index = parseID(input);
        if (index > 0 && index <= values.size)
            return values.get(index - 1);

        return values.find(filter);
    }

    private static <T> T findArray(String input, T[] values, Boolf<T> filter) {
        int index = parseID(input);
        if (index > 0 && index <= values.length)
            return values[index - 1];

        return Structs.find(values, filter);
    }

    private static int parseID(String input) {
        return Strings.parseInt(input.startsWith("#") ? input.substring(1) : input);
    }

    private static boolean canParseID(String input) {
        return Strings.canParseInt(input.startsWith("#") ? input.substring(1) : input);
    }

    // endregion
}