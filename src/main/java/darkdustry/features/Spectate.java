package darkdustry.features;

import arc.Events;
import arc.struct.*;
import arc.util.Timer;
import lombok.AllArgsConstructor;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Posc;
import useful.Bundle;

import static darkdustry.config.Config.config;

public class Spectate {
    @AllArgsConstructor
    private static class Tracker {
        public float x;
        public float y;
        public int tick;

        public int addTick() {
            tick += 1;
            return tick;
        }

        public void move(Posc posc) { x = posc.x(); y = posc.y(); }
        public void pause() { tick = -1; }
        public void reset() { tick = 0; }
        public boolean paused() { return tick < 0; }
    }

    private Spectate() {}

    private final static ObjectMap<Player, Tracker> trackers = new ObjectMap<>();
    private final static ObjectMap<Player, Team> players = new ObjectMap<>();

    private static Tracker tracker(Player player) {
        return trackers.get(player, () -> new Tracker(player.x, player.y, 0));
    }

    public static void init() {
        if (!config.mode.enableSpectate) return;

        Events.on(EventType.PlayerLeave.class, event -> {
            players.remove(event.player);
            trackers.remove(event.player);
        });

        Events.on(EventType.PlayEvent.class, event -> {
            trackers.clear();
        });

        Events.on(EventType.BlockBuildBeginEvent.class, event -> {
            var player = event.unit.getPlayer();
            if (player == null) return;
            tracker(player).pause();
        });

        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            var player = event.unit.getPlayer();
            if (player == null) return;
            tracker(player).reset();
        });

        Events.on(EventType.PlayerChatEvent.class, event -> tracker(event.player).tick = 0);

        Seq<Player> toRemove = new Seq<>();

        Timer.schedule(() -> {
            for (var player : players.entries()) {
                if (player.key.team() != config.mode.spectatorTeam) {
                    Bundle.send(player.key, "commands.spectate.return");
                    toRemove.add(player.key);
                }
            }

            for (var player : toRemove) {
                players.remove(player);
            }
            toRemove.clear();
        }, 0.5f, 0.5f);

        Timer.schedule(() -> {
            if (Vars.state.isPaused()) return;

            Groups.player.each(player -> {
                if (player.team().data().players.count(x -> true) <= 1) return;

                var tracker = tracker(player);
                if (tracker.paused()) return;

                if (Math.abs(player.x - tracker.x) + Math.abs(player.y - tracker.y) > 1) {
                    tracker.move(player);
                    tracker.reset();
                }

                if (tracker.addTick() > 300 && !isSpectator(player)) {
                    spectate(player);
                }
            });
        }, 1f, 1f);
    }

    public static boolean possiblySpectator(Player player) {
        return config.mode.enableSpectate && players.containsKey(player);
    }

    public static boolean isSpectator(Player player) {
        return possiblySpectator(player) && player.team() == config.mode.spectatorTeam;
    }

    public static void spectate(Player player) {
        if (isSpectator(player)) return;

        var decoy = UnitTypes.alpha.spawn(player.team(), 0, 0);
        decoy.controller(player);
        decoy.spawnedByCore(true);

        players.put(player, player.team());
        player.team(config.mode.spectatorTeam);

        player.clearUnit();

        Bundle.send(player, "commands.spectate.success");
    }

    public static void stopSpectating(Player player) {
        if (!isSpectator(player)) return;

        var team = players.remove(player);
        player.team(team);
        Bundle.send(player, "commands.spectate.return");
    }
}
