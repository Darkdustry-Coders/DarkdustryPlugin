package pandorum.commands.client;

import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.comp.Icons;
import pandorum.util.Utils;

import static pandorum.util.Search.*;

public class UnitCommand {
    public static void run(final String[] args, final Player player) {
        UnitType type = findUnit(args[0]);
        if (type == null) {
            Utils.bundled(player, "commands.unit-not-found", Icons.unitsList());
            return;
        }

        Player target = args.length > 1 ? findPlayer(args[1]) : player;
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        Unit unit = type.spawn(target.team(), target.x, target.y);
        target.unit(unit);
        unit.spawnedByCore(true);
        Utils.bundled(target, "commands.admin.unit.success", Icons.get(type.name));
        if (target != player) Utils.bundled(player, "commands.admin.unit.changed", target.coloredName(), Icons.get(type.name));
    }
}
