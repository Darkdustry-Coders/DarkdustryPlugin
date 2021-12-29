package pandorum.commands.client;

import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;

import static pandorum.Misc.*;
import static pandorum.PandorumPlugin.spectating;

public class TeamCommand {
    public static void run(final String[] args, final Player player) {
        Team team = findTeam(args[0]);
        if (team == null) {
            StringBuilder teams = new StringBuilder();
            Structs.each(t -> teams.append("\n[gold] - [white]").append(colorizedTeam(t)), Team.baseTeams);
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        Player target = args.length > 1 ? findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found", args[1]);
            return;
        }

        if (spectating.containsKey(target.uuid())) {
            spectating.remove(target.uuid());
            bundled(target, "commands.admin.spectate.disabled");
        }
        target.team(team);
        bundled(target, "commands.admin.team.success", colorizedTeam(team));
    }
}
