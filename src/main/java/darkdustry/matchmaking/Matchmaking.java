package darkdustry.matchmaking;

import arc.struct.IntMap;
import arc.util.Timer;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class Matchmaking {
    private static final IntMap<ArenaTask> arenasPending = new IntMap<>();

    public static void online(int serverId) {
        var server = arenasPending.get(serverId);
        if (server == null) return;
        server.online();
        arenasPending.remove(serverId);
    }

    public static void setup() {
        Timer.schedule(() -> {
            for (var entry : arenasPending.entries())
                if (!entry.value.isRunning())
                    arenasPending.remove(entry.key);
        }, 5f, 5f);

        Timer.schedule(() -> {
            var players = Groups.player.copy();
            if (players.size < 2) return;

            var first = players.random();
            players.remove(first);
            var second = players.random();

            var ps = new Player[] { first, second };

            new ArenaFindFreeTask(ps).attempt();
        }, 30f, 30f);
    }

    public static void wait(ArenaTask task) {
        arenasPending.put(task.serverId, task);
    }
}