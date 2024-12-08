package darkdustry.features;

import arc.Events;
import darkdustry.database.Cache;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.blocks.storage.CoreBlock;
import useful.Bundle;

import javax.annotation.Nullable;

import static darkdustry.config.Config.config;
import static mindustry.Vars.*;
import static mindustry.server.ServerControl.instance;

public class OnevAll {
    public static @Nullable Player single = null;
    public static @Nullable Player nextSingle = null;
    public static boolean gameOverFlag = false;

    private static @Nullable Team team;

    public static boolean enabled() {
        return config.mode.enable1va && single != null && !gameOverFlag;
    }

    public static void init() {
        if (!config.mode.enable1va) return;

        // Boolean flags:
        // 0 -> Destroying cores
        boolean[] flags = new boolean[] { false };

        Events.on(EventType.PlayEvent.class, event -> {
            flags[0] = false;
        });
    }

    public static void nextMap() {
        single = nextSingle;
        nextSingle = null;
        team = null;
        gameOverFlag = false;

        // if (!enabled()) return;

        // for (var team : Team.all) {
        //     if (team == Team.derelict || team == state.rules.defaultTeam || team == otherTeam()) continue;
        //     if (team.core() == null) continue;

        //     Groups.build.each(x -> x.team == team, x -> x.team(team));
        // }
    }

    private static Team otherTeam() {
        if (team != null) return team;
        for (Team team : Team.all) {
            if (team == state.rules.defaultTeam || team == Team.derelict || team.core() == null) continue;
            OnevAll.team = team;
            return team;
        }
        OnevAll.team = state.rules.waveTeam;
        return state.rules.waveTeam;
    }

    public static Team selectTeam(Player player) {
        return player == single ? state.rules.defaultTeam : otherTeam();
    }

    public static void dipped() {
        assert single != null;
        Bundle.send("1va.dipped", single.coloredName());
        single = null;
        instance.play(() -> maps.getNextMap(instance.lastMode, state.map));
    }

    public static void victory() {
        assert single != null;
        Bundle.send("1va.victory", single.coloredName());
        switch (config.mode) {
            case forts -> Cache.get(single).fortsOvas++;
            case pvp -> Cache.get(single).pvpOvas++;
        }
        single = null;
    }

    public static void defeat() {
        assert single != null;
        Bundle.send("1va.defeat", single.coloredName());
        single = null;
    }
}
