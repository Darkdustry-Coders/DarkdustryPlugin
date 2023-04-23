package darkdustry.features.votes;

import arc.files.Fi;
import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import useful.Bundle;

import static darkdustry.PluginVars.mapLoadDelay;
import static darkdustry.utils.Utils.reloadWorld;

public class VoteLoad extends VoteSession {

    public final Fi target;

    public VoteLoad(Fi target) {
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        Bundle.send("commands.loadsave.vote", player.coloredName(), target.nameWithoutExtension(), votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            Bundle.send("commands.loadsave.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.loadsave.passed", target.nameWithoutExtension(), mapLoadDelay);
        Timer.schedule(() -> reloadWorld(() -> SaveIO.load(target)), mapLoadDelay);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.loadsave.failed", target.nameWithoutExtension());
    }
}