package darkdustry.features;

import arc.Events;
import arc.util.Timer;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Player;
import useful.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static darkdustry.config.Config.config;

public class Spectate {
    private Spectate() {}

    private final static HashMap<Player, Team> players = new HashMap<>();

    public static void init() {
        Events.on(EventType.PlayerLeave.class, event -> players.remove(event.player));
        Events.on(EventType.PlayEvent.class, event -> {
            for (var entry : players.entrySet()) {
                Bundle.send(entry.getKey(), "commands.spectate.return");
            }
            players.clear();
        });

        List<Player> toRemove = new ArrayList<>();

        Timer.schedule(() -> {
            for (var player : players.entrySet()) {
                if (player.getKey().team() != config.mode.spectatorTeam) {
                    Bundle.send(player.getKey(), "commands.spectate.return");
                    toRemove.add(player.getKey());
                }
            }
            for (var player : toRemove) {
                players.remove(player);
            }
            toRemove.clear();
        }, 0.5f, 0.5f);
    }

    public static boolean isSpectator(Player player) {
        return config.mode.enableSpectate && player.team() == config.mode.spectatorTeam && players.containsKey(player);
    }

    public static void spectate(Player player) {
        if (isSpectator(player)) return;

        var decoy = UnitTypes.alpha.spawn(player.team(), 0, 0);
        decoy.controller(player);
        players.put(player, player.team());
        player.team(config.mode.spectatorTeam);
        var unit = player.unit();
        if (unit != null) unit.kill();
        Bundle.send(player, "commands.spectate.success");
    }

    public static void stopSpectating(Player player) {
        if (!isSpectator(player)) return;

        var team = players.remove(player);
        player.team(team);
        Bundle.send(player, "commands.spectate.return");
    }
}
