package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.util.Utils;

import static pandorum.PluginVars.activeSpectatingPlayers;
import static pandorum.util.Search.findPlayer;
import static pandorum.util.Search.findTeam;
import static pandorum.util.Utils.teamsList;

public class TeamCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Team team = findTeam(args[0]);
        if (team == null) {
            Utils.bundled(player, "commands.team-not-found", teamsList());
            return;
        }

        Player target = args.length > 1 ? findPlayer(args[1]) : player;
        if (target == null) {
            Utils.bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        if (activeSpectatingPlayers.containsKey(target.uuid())) {
            activeSpectatingPlayers.remove(target.uuid());
            Utils.bundled(target, "commands.admin.spectate.success.disabled");
        }

        target.team(team);
        Utils.bundled(target, "commands.admin.team.success", Utils.coloredTeam(team));
        if (target != player)
            Utils.bundled(player, "commands.admin.team.changed", target.coloredName(), Utils.coloredTeam(team));
    }
}
