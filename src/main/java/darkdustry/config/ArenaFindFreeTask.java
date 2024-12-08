package darkdustry.config;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Player;

public class ArenaFindFreeTask {
    private final Player[] players;
    private int tries = 10;
    private int tryPort = -1;

    public ArenaFindFreeTask(Player[] players) {
        this.players = players;
    }

    private void submit() {
        int serverId = Mathf.random(1, Integer.MAX_VALUE);
        new ArenaTask(players, tryPort, serverId).setup();
    }

    public void attempt() {
        if (--tries <= 0) return;

        for (var player : players) if (!player.con.isConnected()) return;

        tryPort = Mathf.random(MatchmakingConfig.config.portRangeStart, MatchmakingConfig.config.portRangeEnd);
        Vars.net.pingHost("127.0.0.1", tryPort, ignored -> submit(), ignored -> attempt());
    }
}
