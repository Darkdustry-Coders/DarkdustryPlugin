package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.content.UnitTypes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import pandorum.components.Icons;
import pandorum.util.Utils;

import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findUnit;

public class UnitCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        UnitType type = findUnit(args[0]);
        if (type == null || type == UnitTypes.block) {
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
        if (target != player)
            Utils.bundled(player, "commands.admin.unit.changed", target.coloredName(), Icons.get(type.name));
    }
}
