package pandorum.commands.client;

import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.comp.Icons;

import static mindustry.Vars.content;
import static pandorum.Misc.*;

public class UnitCommand {
    public static void run(final String[] args, final Player player) {
        UnitType type = findUnit(args[0]);
        if (type == null) {
            StringBuilder units = new StringBuilder();
            content.units().each(u -> u != UnitTypes.block, u -> units.append(" ").append(Icons.get(u.name)).append(u.name));
            bundled(player, "commands.unit-not-found", units.toString());
            return;
        }

        Player target = args.length > 1 ? findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        Unit unit = type.spawn(player.team(), player.x(), player.y());
        unit.spawnedByCore(true);
        target.unit(unit);
        bundled(target, "commands.admin.unit.success", Icons.get(type.name));
    }
}
