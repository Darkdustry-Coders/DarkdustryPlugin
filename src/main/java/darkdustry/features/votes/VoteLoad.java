package darkdustry.features.votes;

import arc.files.Fi;
import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;

public class VoteLoad extends VoteSession {

    public final Fi file;

    public VoteLoad(Fi file) {
        this.file = file;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send("commands.loadsave.vote", player.coloredName(), file.nameWithoutExtension(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player) != 0)
            Bundle.send("commands.loadsave.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.loadsave.success", file.nameWithoutExtension(), mapLoadDelay);
        Timer.schedule(() -> reloadWorld(() -> SaveIO.load(file)), mapLoadDelay);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.loadsave.fail", file.nameWithoutExtension());
    }
}