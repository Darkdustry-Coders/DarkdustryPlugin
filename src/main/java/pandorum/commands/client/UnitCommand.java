package pandorum.commands.client;

import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.admin.RequireAdmin;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.bundled;

public class UnitCommand {
    @RequireAdmin
    @ClientCommand(name = "unit", args = "<unit> [player...]", description = "Change a unit.")
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        UnitType type = content.units().find(u -> u.name.equalsIgnoreCase(args[0]) && u != UnitTypes.block);
        if (type == null) {
            StringBuilder units = new StringBuilder();
            content.units().each(u -> u != UnitTypes.block, u -> units.append(" ").append(Icons.get(u.name)).append(u.name));
            bundled(player, "commands.unit-not-found", units.toString());
            return;
        }

        Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        Unit unit = type.spawn(player.team(), player.x(), player.y());
        unit.spawnedByCore(true);
        target.unit(unit);
        bundled(target, "commands.admin.unit.success", Icons.get(type.name));
    }
}
