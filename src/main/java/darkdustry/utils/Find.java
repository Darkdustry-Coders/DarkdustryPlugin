package darkdustry.utils;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Structs;
import darkdustry.features.Ranks.Rank;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Locale;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.utils.Utils.*;

public class Find {

    public static Locale locale(String name) {
        return notNullElse(supportedLocales.find(locale -> name.startsWith(locale.toString())), defaultLocale);
    }

    public static Player player(String name) {
        return canParsePositiveInt(name) ? Groups.player.getByID(parseInt(name)) : Groups.player.find(player -> deepEquals(player.name, name));
    }

    public static PlayerInfo playerInfo(String name) {
        var player = player(name);
        if (player != null) return player.getInfo();

        return Utils.notNullElse(netServer.admins.getInfoOptional(name), netServer.admins.findByIP(name));
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
        return canParsePositiveInt(name) ? Rank.ranks.get(parseInt(name)) : Rank.ranks.find(rank -> rank.name.equalsIgnoreCase(name));
    }

    public static Map map(String name) {
        var list = maps.customMaps();
        return parseInt(name) > 0 && parseInt(name) <= list.size ? list.get(parseInt(name) - 1) : list.find(map -> deepEquals(map.name(), name));
    }

    public static Fi save(String name) {
        var list = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        return parseInt(name) > 0 && parseInt(name) <= list.size ? list.get(parseInt(name) - 1) : list.find(save -> deepEquals(save.nameWithoutExtension(), name));
    }

    public static Gamemode mode(String name) {
        return Structs.find(Gamemode.all, mode -> mode.name().equalsIgnoreCase(name));
    }

    public static Block core(String name) {
        return content.blocks().select(block -> block instanceof CoreBlock).find(block -> block.name.split("-")[1].equalsIgnoreCase(name));
    }

    public static String language(String name) {
        if (mindustry2Api.containsKey(name)) return mindustry2Api.get(name);
        return Utils.notNullElse(translatorLanguages.keys().toSeq().find(name::startsWith), defaultLanguage);
    }
}
