package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.components.Icons;
import pandorum.util.Utils;

import static pandorum.util.Search.findCore;
import static pandorum.util.Search.findTeam;
import static pandorum.util.Utils.teamsList;

public class CoreCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Block core = args.length > 0 ? findCore(args[0]) : Blocks.coreShard;
        if (core == null) {
            Utils.bundled(player, "commands.admin.core.core-not-found");
            return;
        }

        Team team = args.length > 1 ? findTeam(args[1]) : player.team();
        if (team == null) {
            Utils.bundled(player, "commands.team-not-found", teamsList());
            return;
        }

        Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, team, false);
        Utils.bundled(player, player.tileOn() != null && player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed", Icons.get(core.name), Utils.coloredTeam(team));
    }
}
