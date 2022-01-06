package pandorum.commands.client;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static pandorum.Misc.*;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.votesSurrender;

public class SurrenderCommand {
    public static void run(final String[] args, final Player player) {
        Seq<String> teamVotes = votesSurrender.get(player.team(), Seq::new);
        if (teamVotes.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        teamVotes.add(player.uuid());
        int cur = teamVotes.size;
        int req = Mathf.ceil(config.voteRatio * Groups.player.count(p -> p.team() == player.team()));
        sendToChat("commands.surrender.vote", colorizedTeam(player.team()), player.coloredName(), cur, req);

        if (cur < req) return;

        sendToChat("commands.surrender.passed", colorizedTeam(player.team()));
        votesSurrender.remove(player.team());
        player.team().data().destroyToDerelict();
    }
}
