package darkdustry.features.votes;

import mindustry.gen.Player;

import static mindustry.Vars.logic;
import static useful.Bundle.sendToChat;

public class VoteVnw extends VoteSession {

    public final int waves;

    public VoteVnw(int waves) {
        this.waves = waves;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.vnw.vote", player.coloredName(), waves, votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            sendToChat("commands.vnw.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.vnw.passed", waves);
        for (int i = 0; i < waves; i++) logic.skipWave();
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.vnw.failed", waves);
    }
}