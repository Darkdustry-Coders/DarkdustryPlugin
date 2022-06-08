package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import pandorum.components.Icons;
import pandorum.util.StringUtils;

import static pandorum.PluginVars.maxSpawnAmount;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findTeam;
import static pandorum.util.Search.findUnit;
import static pandorum.util.StringUtils.teamsList;
import static pandorum.util.StringUtils.unitsList;

public class SpawnCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        if (args.length > 1 && !Strings.canParseInt(args[1])) {
            bundled(player, "commands.non-int");
            return;
        }

        UnitType type = findUnit(args[0]);
        if (type == null || type == UnitTypes.block) {
            bundled(player, "commands.unit-not-found", unitsList());
            return;
        }

        int amount = args.length > 1 ? Strings.parseInt(args[1]) : 1;
        if (amount > maxSpawnAmount || amount < 1) {
            bundled(player, "commands.admin.spawn.limit", maxSpawnAmount);
            return;
        }

        Team team = args.length > 2 ? findTeam(args[2]) : player.team();
        if (team == null) {
            bundled(player, "commands.team-not-found", teamsList());
            return;
        }

        for (int i = 0; i < amount; i++) type.spawn(team, player.x, player.y);
        bundled(player, "commands.admin.spawn.success", amount, Icons.get(type.name), StringUtils.coloredTeam(team));
    }
}
