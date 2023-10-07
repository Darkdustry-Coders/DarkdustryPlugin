package darkdustry.features.votes;

import mindustry.gen.Player;
import useful.Bundle;

import static mindustry.Vars.*;

public class VoteVnw extends VoteSession {

    public final int waves;

    public VoteVnw(int waves) {
        this.waves = waves;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send(sign == 1 ? "commands.vnw.yes" : "commands.vnw.no", player.coloredName(), waves, votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (votes.remove(player) != 0)
            Bundle.send("commands.vnw.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.vnw.success", waves);

        for (int i = 0; i < waves; i++)
            logic.runWave();
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.vnw.fail", waves);
    }
}