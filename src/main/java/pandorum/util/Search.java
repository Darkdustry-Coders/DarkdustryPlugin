package pandorum.util;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.PlayerInfo;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.components.Bundle;
import pandorum.features.Ranks.Rank;

import java.util.Locale;

import static mindustry.Vars.*;
import static pandorum.PluginVars.*;

public class Search {

    public static Map findMap(String name) {
        Seq<Map> mapsList = maps.customMaps();
        return Strings.canParsePositiveInt(name) && Strings.parseInt(name) < mapsList.size ? mapsList.get(Strings.parseInt(name)) : mapsList.find(map -> Utils.deepEquals(map.name(), name));
    }

    public static Fi findSave(String name) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(SaveIO::isSaveValid);
        return Strings.canParsePositiveInt(name) && Strings.parseInt(name) < savesList.size ? savesList.get(Strings.parseInt(name)) : savesList.find(save -> Utils.deepEquals(save.nameWithoutExtension(), name));
    }

    public static Rank findRank(String name) {
        return Strings.canParsePositiveInt(name) && Strings.parseInt(name) < Rank.ranks.size ? Rank.ranks.get(Strings.parseInt(name)) : Rank.ranks.find(rank -> rank.name.equalsIgnoreCase(name));
    }

    public static Locale findLocale(String name) {
        return Utils.notNullElse(Structs.find(Bundle.supportedLocales, locale -> name.equals(locale.toString()) || name.startsWith(locale.toString())), Bundle.defaultLocale);
    }

    public static String findTranslatorLanguage(String name) {
        if (mindustryLocales2Api.containsKey(name)) return mindustryLocales2Api.get(name);

        return Utils.notNullElse(translatorLanguages.keys().toSeq().find(language -> name.equals(language) || name.startsWith(language)), defaultLanguage);
    }

    public static Player findPlayer(String name) {
        Player target = Groups.player.find(player -> Utils.deepEquals(name, player.name));

        if (target == null) {
            target = Groups.player.getByID(Strings.parseInt(name));
        }

        return target;
    }

    public static Block findBlock(String name) {
        return Strings.canParsePositiveInt(name) ? content.block(Strings.parseInt(name)) : content.blocks().find(block -> block.name.equalsIgnoreCase(name));
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

    public static PlayerInfo findPlayerInfo(String name) {
        Player player = findPlayer(name);
        return player != null ? player.getInfo() : netServer.admins.getInfoOptional(name);
    }
}
