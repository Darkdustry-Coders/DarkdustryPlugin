package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.util.Utils;

import static pandorum.PluginVars.*;

public class SurrenderCommand {
    public static void run(final String[] args, final Player player) {
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
        player.team().data().destroyToDerelict();
    }
}
