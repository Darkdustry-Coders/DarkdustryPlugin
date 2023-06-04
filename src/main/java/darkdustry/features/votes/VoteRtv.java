package darkdustry.features.votes;

import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.maps.Map;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class VoteRtv extends VoteSession {

    public final Map map;

    public VoteRtv(Map map) {
        this.map = map;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send("commands.rtv.vote", player.coloredName(), map.name(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player) != 0)
            Bundle.send("commands.rtv.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.rtv.passed", map.name(), mapLoadDelay);
        Timer.schedule(() -> reloadWorld(() -> world.loadMap(map, map.applyRules(state.rules.mode()))), mapLoadDelay);
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.rtv.failed", map.name());
    }
}