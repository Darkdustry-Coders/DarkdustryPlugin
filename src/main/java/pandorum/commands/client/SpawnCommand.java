package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.components.Icons;
import pandorum.util.Utils;

import static pandorum.PluginVars.maxSpawnAmount;
import static pandorum.util.Search.findTeam;
import static pandorum.util.Search.findUnit;
import static pandorum.util.Utils.teamsList;
import static pandorum.util.Utils.unitsList;

public class SpawnCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            Utils.bundled(player, "commands.non-int");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null || type == UnitTypes.block) {
            Utils.bundled(player, "commands.unit-not-found", unitsList());
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount > maxSpawnAmount || amount < 1) {
            Utils.bundled(player, "commands.admin.spawn.limit", maxSpawnAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            Utils.bundled(player, "commands.team-not-found", teamsList());
            return;
        }

        for (int i = 0; i < amount; i++) type.spawn(team, player.x, player.y);
        Utils.bundled(player, "commands.admin.spawn.success", amount, Icons.get(type.name), Utils.coloredTeam(team));
    }
}
