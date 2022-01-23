package pandorum.commands.client;

import arc.Events;
import arc.math.Mathf;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.voteRatio;
import static pandorum.PluginVars.votesRtv;

public class RtvCommand {
    public static void run(final String[] args, final Player player) {
        if (votesRtv.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        votesRtv.add(player.uuid());
        int cur = votesRtv.size;
        int req = Mathf.ceil(voteRatio * Groups.player.size());
        sendToChat("commands.rtv.vote", player.coloredName(), cur, req);

        if (cur < req) return;

        sendToChat("commands.rtv.passed");
        votesRtv.clear();
        Events.fire(new GameOverEvent(state.rules.waveTeam));
    }
}
