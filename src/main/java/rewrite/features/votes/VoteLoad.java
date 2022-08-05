package rewrite.features.votes;

import arc.files.Fi;
import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static rewrite.components.Bundle.*;
import static rewrite.utils.Utils.*;

public class VoteLoad extends VoteSession {

    public final Fi target;

    public VoteLoad(Fi target) {
        super();
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.nominate.load.vote", player.name, target.nameWithoutExtension(), votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.nominate.load.passed", target.nameWithoutExtension());
        Timer.schedule(() -> load(() -> SaveIO.load(target)), 10f);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.nominate.load.failed", target.nameWithoutExtension());

    }
}
