package darkdustry.features;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import arc.util.Timer.Task;
import mindustry.game.Team;
import mindustry.gen.Player;

public class RainbowTeam {

    public static final IntMap<Task> rainbowCache = new IntMap<>();

    public static Seq<Team> rainbowTeams;

    public static void load() {
        rainbowTeams = Seq.with(Team.all).filter(team -> {
            var hsv = Color.RGBtoHSV(team.color);
            return hsv[2] > 85;
        }).sort(team -> {
            var hsv = Color.RGBtoHSV(team.color);
            return hsv[0] * 1000 + hsv[1];
        });
    }

    public static void add(Player player) {
        var task = Timer.schedule(() -> player.team(rainbowTeams.get(Mathf.floor(Time.time / 6f % rainbowTeams.size))), 0f, 0.1f);
        rainbowCache.put(player.id, task);
    }

    public static void remove(Player player) {
        var task = rainbowCache.remove(player.id);
        if (task != null) task.cancel();
    }
}