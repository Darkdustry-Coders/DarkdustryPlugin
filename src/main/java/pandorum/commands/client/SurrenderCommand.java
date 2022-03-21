package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.util.Utils;

import static mindustry.Vars.world;
import static pandorum.PluginVars.*;

public class SurrenderCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Seq<String> teamVotes = votesSurrender.get(player.team(), Seq::new);
        if (teamVotes.contains(player.uuid())) {
            Utils.bundled(player, "commands.already-voted");
            return;
        }

        if (!canVote) {
            Utils.bundled(player, "commands.can-not-vote");
            return;
        }

        teamVotes.add(player.uuid());
        int cur = teamVotes.size;
        int req = Mathf.ceil(voteRatio * Groups.player.count(p -> p.team() == player.team()));
        Utils.sendToChat("commands.surrender.vote", Utils.coloredTeam(player.team()), player.coloredName(), cur, req);

        if (cur < req) return;

        Utils.sendToChat("commands.surrender.passed", Utils.coloredTeam(player.team()));
        votesSurrender.remove(player.team());

        world.tiles.eachTile(tile -> {
            if (tile.build != null && tile.block() != Blocks.air && tile.team() == player.team()) {
                Time.run(Mathf.random(360f), tile::removeNet);
            }
        });

        player.team().data().units.each(unit -> Time.run(Mathf.random(360f), () -> Call.unitDespawn(unit)));
    }
}
