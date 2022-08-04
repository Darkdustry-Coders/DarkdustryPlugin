package rewrite.features.votes;

import arc.files.Fi;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static arc.Core.*;
import static rewrite.components.Bundle.*;

public class VoteSave extends VoteSession {

    public final Fi target;

    public VoteSave(Fi target) {
        super();
        this.target = target;
    }

    @Override
    public void vote(Player player, int sing) {
        super.vote(player, sing);
        sendToChat("commands.nominate.save.vote", player.name, target.nameWithoutExtension(), votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.nominate.save.passed", target.nameWithoutExtension());
        app.post(() -> SaveIO.save(target));
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.nominate.save.failed", target.nameWithoutExtension());
    }
}
