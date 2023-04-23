package darkdustry.features.votes;

import mindustry.gen.Player;
import useful.Bundle;

import static mindustry.Vars.logic;

public class VoteVnw extends VoteSession {

    public final int waves;

    public VoteVnw(int waves) {
        this.waves = waves;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        Bundle.send("commands.vnw.vote", player.coloredName(), waves, votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            Bundle.send("commands.vnw.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.vnw.passed", waves);

        for (int i = 0; i < waves; i++)
            logic.runWave();
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.vnw.failed", waves);
    }
}