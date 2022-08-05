package rewrite.features.votes;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.gen.Player;

import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;

public class VoteRtv extends VoteSession {

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.rtv.vote", player.name, votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.rtv.passed");
        Events.fire(new GameOverEvent(state.rules.waveTeam));
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.rtv.failed");
    }
}
