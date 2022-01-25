package pandorum.commands.client;

import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.comp.Icons;
import pandorum.utils.Utils;

import static pandorum.utils.Search.*;
import static pandorum.utils.Utils.bundled;

public class CoreCommand {
    public static void run(final String[] args, final Player player) {
        Block core = args.length > 0 ? findCore(args[0]) : Blocks.coreShard;
        if (core == null) {
            bundled(player, "commands.admin.core.core-not-found");
            return;
        }

        Team team = args.length > 1 ? findTeam(args[1]) : player.team();
        if (team == null) {
            bundled(player, "commands.team-not-found", Icons.teamsList());
            return;
        }

        Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
        bundled(player, player.tileOn() != null && player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed", Icons.get(core.name), Utils.colorizedTeam(team));
    }
}
