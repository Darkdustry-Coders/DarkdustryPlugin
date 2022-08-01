package rewrite.utils;

import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;

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

    public static Block core(String name) {
        return switch (name.toLowerCase()) {
            case "shard" -> Blocks.coreShard;
            case "foundation" -> Blocks.coreFoundation;
            case "nucleus" -> Blocks.coreNucleus;
            case "bastion" -> Blocks.coreBastion;
            case "citadel" -> Blocks.coreCitadel;
            case "acropolis" -> Blocks.coreAcropolis;
            default -> null; // нормальный метод, не кипишуй дарк
        };
    }
}
