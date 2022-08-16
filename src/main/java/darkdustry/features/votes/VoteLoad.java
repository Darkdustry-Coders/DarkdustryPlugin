package darkdustry.features.votes;

import arc.files.Fi;
import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static darkdustry.PluginVars.mapLoadTime;
import static darkdustry.components.Bundle.*;
import static darkdustry.utils.Utils.*;

public class VoteLoad extends VoteSession {

    public final Fi target;

    public VoteLoad(Fi target) {
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.loadsave.vote", player.coloredName(), target.nameWithoutExtension(), votes(), votesRequired());
        if (votes() >= votesRequired()) success();
    }

    @Override
    public void left(Player player) {
        super.left(player);
        sendToChat("commands.loadsave.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.loadsave.passed", target.nameWithoutExtension(), mapLoadTime);
        Timer.schedule(() -> reloadWorld(() -> SaveIO.load(target)), mapLoadTime);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.loadsave.failed", target.nameWithoutExtension());
    }
}
