package rewrite.features.votes;

import mindustry.gen.Player;

import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;

public class VoteVnw extends VoteSession {

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.rtv.vote", player.name, votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.rtv.passed");
        state.wavetime = 0f;
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.rtv.failed");
    }
}
