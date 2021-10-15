package pandorum.commands.client;

import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class UnitsCommand {
    public static void run(final String[] args, final Player player) {
        switch (args[0]) {
            case "name" -> {
                if (!player.dead()) bundled(player, "commands.unit-name", player.unit().type().name);
                else bundled(player, "commands.unit-name.null");
            }
            case "list" -> {
                StringBuilder units = new StringBuilder();
                content.units().each(unit -> {
                    if (!unit.name.equals("block")) units.append(" ").append(Icons.icons.get(unit.name)).append(unit.name);
                });
                bundled(player, "commands.units.list", units.toString());
            }
            case "change" -> {
                if (Misc.adminCheck(player)) return;
                if (args.length == 1 || args[1].equals("block")) {
                    bundled(player, "commands.units.incorrect");
                    return;
                }
                UnitType found = content.units().find(u -> u.name.equalsIgnoreCase(args[1]));
                if (found == null) {
                    bundled(player, "commands.unit-not-found");
                    return;
                }
                Unit spawn = found.spawn(player.team(), player.x(), player.y());
                spawn.spawnedByCore(true);
                player.unit(spawn);
                bundled(player, "commands.units.change.success");
            }
            default -> bundled(player, "commands.units.incorrect");
        }
    }
}
