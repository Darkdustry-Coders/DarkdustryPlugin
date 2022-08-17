package darkdustry.features.votes;

import arc.files.Fi;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static arc.Core.*;
import static darkdustry.components.Bundle.*;

public class VoteSave extends VoteSession {

    public final Fi target;

    public VoteSave(Fi target) {
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.savemap.vote", player.coloredName(), target.nameWithoutExtension(), votes(), votesRequired());
        if (votes() >= votesRequired()) success();
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.uuid()) != 0)
            sendToChat("commands.savemap.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.savemap.passed", target.nameWithoutExtension());
        app.post(() -> SaveIO.save(target));
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.savemap.failed", target.nameWithoutExtension());
    }
}
