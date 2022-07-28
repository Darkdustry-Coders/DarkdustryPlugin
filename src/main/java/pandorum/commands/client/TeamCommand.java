package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.util.Utils;

import static pandorum.PluginVars.activeSpectatingPlayers;
import static pandorum.PluginVars.teamsList;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findTeam;

public class TeamCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!player.admin) {
            bundled(player, "commands.permission-denied");
            return;
        }

        Team team = findTeam(args[0]);
        if (team == null) {
            bundled(player, "commands.team-not-found", teamsList);
            return;
        }

        Player target = args.length > 1 ? findPlayer(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        if (activeSpectatingPlayers.containsKey(target.uuid())) {
            activeSpectatingPlayers.remove(target.uuid());
            bundled(target, "commands.spectate.success.disabled");
        }

        target.team(team);
        bundled(target, "commands.team.success", Utils.coloredTeam(team));
        if (target != player)
            bundled(player, "commands.team.success.player", target.name, Utils.coloredTeam(team));
    }
}
