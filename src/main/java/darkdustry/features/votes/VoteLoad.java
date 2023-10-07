package darkdustry.features.votes;

import arc.files.Fi;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import useful.Bundle;

import static mindustry.net.Administration.Config.*;
import static mindustry.server.ServerControl.*;

public class VoteLoad extends VoteSession {

    public final Fi file;

    public VoteLoad(Fi file) {
        this.file = file;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send(sign == 1 ? "commands.voteload.yes" : "commands.voteload.no", player.coloredName(), file.nameWithoutExtension(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (votes.remove(player) != 0)
            Bundle.send("commands.voteload.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.voteload.success", file.nameWithoutExtension(), roundExtraTime.num());

        instance.play(() -> SaveIO.load(file));
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.voteload.fail", file.nameWithoutExtension());
    }
}