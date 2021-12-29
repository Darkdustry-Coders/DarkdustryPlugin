package pandorum.commands.client;

import arc.util.Structs;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.comp.Icons;

import static pandorum.Misc.*;

public class CoreCommand {
    public static void run(final String[] args, final Player player) {
        Block core = findCore(args[0]);
        if (core == null) {
            bundled(player, "commands.admin.core.core-not-found");
            return;
        }

        Team team = args.length > 1 ? findTeam(args[1]) : player.team();
        if (team == null) {
            StringBuilder teams = new StringBuilder();
            Structs.each(t -> teams.append("\n[gold] - [white]").append(colorizedTeam(t)), Team.baseTeams);
            bundled(player, "commands.team-not-found", teams.toString());
            return;
        }

        Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
        bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed", Icons.get(core.name));
    }
}
