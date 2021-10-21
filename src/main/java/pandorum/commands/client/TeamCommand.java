package pandorum.commands.client;

import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Player;
import pandorum.Misc;

import static pandorum.Misc.bundled;

public class TeamCommand {
    public static void run(final String[] args, final Player player) {
        Team team = Structs.find(Team.all, t -> t.name.equalsIgnoreCase(args[0]));
        if (team == null) {
            bundled(player, "commands.teams");
            return;
        }

        Player target = args.length > 1 ? Misc.findByName(args[1]) : player;
        if (target == null) {
            bundled(player, "commands.player-not-found");
            return;
        }

        bundled(target, "commands.admin.team.success", Misc.colorizedTeam(team));
        target.team(team);
    }
}