package pandorum.commands.client;

import arc.Events;
import arc.math.Mathf;
import arc.util.CommandHandler.CommandRunner;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.sendToChat;

public class RtvCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (votesRtv.contains(player.uuid())) {
            bundled(player, "commands.already-voted");
            return;
        }

        if (!canVote) {
            bundled(player, "commands.can-not-vote");
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
