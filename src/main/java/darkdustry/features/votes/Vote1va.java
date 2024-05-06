package darkdustry.features.votes;

import darkdustry.features.OnevAll;
import mindustry.gen.Player;
import mindustry.maps.Map;
import useful.Bundle;

import static mindustry.Vars.world;
import static mindustry.net.Administration.Config.roundExtraTime;
import static mindustry.server.ServerControl.instance;

public class Vote1va extends VoteSession {
    public final Map map;
    public final Player player;

    public Vote1va(Map map, Player player) {
        this.map = map;
        this.player = player;
        Bundle.send("commands.1va.start", player.coloredName(), map.name(), votes() + 1, votesRequired());
        super.vote(player, 1);
    }

    @Override
    public void vote(Player player, int sign) {
        Bundle.send(sign == 1 ? "commands.1va.yes" : "commands.1va.no", player.coloredName(), map.name(), votes() + sign, votesRequired());
        super.vote(player, sign);
    }

    @Override
    public void left(Player player) {
        if (player == this.player) {
            stop();
            Bundle.send("commands.1va.cancel", player.coloredName());
            return;
        }
        if (votes.remove(player) != 0)
            Bundle.send("commands.1va.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        Bundle.send("commands.1va.success", map.name(), roundExtraTime.num());

        OnevAll.nextSingle = player;
        instance.play(() -> world.loadMap(map));
    }

    @Override
    public void fail() {
        stop();
        Bundle.send("commands.1va.fail", map.name());
    }
}
