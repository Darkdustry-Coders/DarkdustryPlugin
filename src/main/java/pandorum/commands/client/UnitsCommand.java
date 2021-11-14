package pandorum.commands.client;

import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class UnitsCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        switch (args[0].toLowerCase()) {
            case "name" -> {
                if (!player.dead()) bundled(player, "commands.admin.unit-name", player.unit().type().name);
            }
            case "list" -> {
                StringBuilder units = new StringBuilder();
                content.units().each(unit -> !unit.name.equals("block"), unit -> units.append(" ").append(Icons.get(unit.name)).append(unit.name));
                bundled(player, "commands.admin.units.list", units.toString());
            }
            case "change" -> {
                if (args.length < 2) {
                    bundled(player, "commands.admin.units.incorrect");
                    return;
                }

                UnitType type = content.units().find(u -> u.name.equalsIgnoreCase(args[1]));
                if (type == null || type == UnitTypes.block) {
                    bundled(player, "commands.unit-not-found");
                    return;
                }

                Player target = args.length > 2 ? Misc.findByName(args[2]) : player;
                if (target == null) {
                    bundled(player, "commands.player-not-found");
                    return;
                }

                Unit unit = type.spawn(player.team(), player.x(), player.y());
                unit.spawnedByCore(true);
                target.unit(unit);
                bundled(player, "commands.admin.units.change.success");
            }
            default -> bundled(player, "commands.admin.units.incorrect");
        }
    }
}
