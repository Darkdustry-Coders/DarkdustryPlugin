package pandorum.commands.client;

import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class UnitsCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        switch (args[0]) {
            case "name" -> {
                if (!player.dead()) bundled(player, "commands.admin.unit-name", player.unit().type().name);
            }
            case "list" -> {
                StringBuilder units = new StringBuilder();
                content.units().each(unit -> !unit.name.equals("block"), unit -> units.append(" ").append(Icons.get(unit.name)).append(unit.name));
                bundled(player, "commands.admin.units.list", units.toString());
            }
            case "change" -> {
                if (args.length == 1 || args[1].equalsIgnoreCase("block")) {
                    bundled(player, "commands.admin.units.incorrect");
                    return;
                }
                UnitType found = content.units().find(u -> u.name.equalsIgnoreCase(args[1]));
                if (found == null) {
                    bundled(player, "commands.unit-not-found");
                    return;
                }
                Unit unit = found.spawn(player.team(), player.x(), player.y());
                unit.spawnedByCore(true);
                player.unit(unit);
                bundled(player, "commands.admin.units.change.success");
            }
            default -> bundled(player, "commands.admin.units.incorrect");
        }
    }
}
