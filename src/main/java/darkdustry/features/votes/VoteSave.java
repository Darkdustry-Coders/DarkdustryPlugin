package darkdustry.features.votes;

import arc.files.Fi;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import useful.Bundle;

public class VoteSave extends VoteSession {

    public final Fi target;

    public VoteSave(Fi target) {
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        Bundle.send("commands.savemap.vote", player.coloredName(), target.nameWithoutExtension(), votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            Bundle.send("commands.savemap.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.savemap.passed", target.nameWithoutExtension());
        SaveIO.save(target);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.savemap.failed", target.nameWithoutExtension());
    }
}