package pandorum.commands.client;

import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.comp.Icons;

import static pandorum.Misc.*;
import static pandorum.PluginVars.maxSpawnAmount;

public class SpawnCommand {
    public static void run(final String[] args, final Player player) {
        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null) {
            bundled(player, "commands.unit-not-found", Icons.unitsList());
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount > maxSpawnAmount || amount < 1) {
            bundled(player, "commands.admin.spawn.limit", maxSpawnAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            bundled(player, "commands.team-not-found", Icons.teamsList());
            return;
        }

        for (int i = 0; i < amount; i++) type.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", amount, Icons.get(type.name), colorizedTeam(team));
    }
}
