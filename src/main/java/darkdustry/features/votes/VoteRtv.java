package darkdustry.features.votes;

import mindustry.gen.Player;
import mindustry.maps.Map;
import useful.Bundle;

import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;
import static mindustry.server.ServerControl.*;

public class VoteRtv extends VoteSession {

    public final Map map;

    public VoteRtv(Map map) {
        this.map = map;
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send(sign == 1 ? "commands.rtv.yes" : "commands.rtv.no", player.coloredName(), map.name(), votes() + sign, votesRequired());
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
        Bundle.send("commands.rtv.success", map.name(), roundExtraTime.num());

        instance.play(() -> world.loadMap(map));
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.rtv.fail", map.name());
    }
}