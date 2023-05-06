package darkdustry.features.votes;

import arc.files.Fi;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import useful.Bundle;

public class VoteSave extends VoteSession {

    public final Fi file;

    public VoteSave(Fi file) {
        this.file = file;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send("commands.savemap.vote", player.coloredName(), file.nameWithoutExtension(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            Bundle.send("commands.savemap.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.savemap.passed", file.nameWithoutExtension());
        SaveIO.save(file);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.savemap.failed", file.nameWithoutExtension());
    }
}