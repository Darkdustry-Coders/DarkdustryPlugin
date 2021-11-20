package pandorum.commands.client;

import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Icons;

import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedTeam;

public class TeamCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
        if (team == null) {
            StringBuilder teams = new StringBuilder();
            for (Team t : Team.baseTeams) teams.append("\n[gold] - [white]").append(Icons.get(t.name)).append(colorizedTeam(t));
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        target.team(team);
        bundled(target, "commands.admin.team.success", colorizedTeam(team));
    }
}
