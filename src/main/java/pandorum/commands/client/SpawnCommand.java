package pandorum.commands.client;

import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.comp.Icons;
import pandorum.utils.Utils;

import static pandorum.utils.Search.*;
import static pandorum.PluginVars.maxSpawnAmount;

public class SpawnCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            Utils.bundled(player, "commands.non-int");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null) {
            Utils.bundled(player, "commands.unit-not-found", Icons.unitsList());
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount > maxSpawnAmount || amount < 1) {
            Utils.bundled(player, "commands.admin.spawn.limit", maxSpawnAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            Utils.bundled(player, "commands.team-not-found", Icons.teamsList());
            return;
        }

        for (int i = 0; i < amount; i++) type.spawn(team, player.x, player.y);
        Utils.bundled(player, "commands.admin.spawn.success", amount, Icons.get(type.name), Utils.colorizedTeam(team));
    }
}
