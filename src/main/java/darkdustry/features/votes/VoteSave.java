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
        Bundle.send(sign == 1 ? "commands.votesave.yes" : "commands.votesave.no", player.coloredName(), file.name(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (votes.remove(player) != 0)
            Bundle.send("commands.votesave.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.votesave.success", file.name());

        SaveIO.save(file);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.votesave.fail", file.name());
    }
}