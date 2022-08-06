package rewrite.features.votes;

import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.maps.Map;

import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static rewrite.components.Bundle.sendToChat;
import static rewrite.utils.Utils.reloadWorld;

public class VoteMap extends VoteSession {

    public final Map target;

    public VoteMap(Map target) {
        super();
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.nominate.map.vote", player.name, target.name(), votes, votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.nominate.map.passed", target.name());
        Timer.schedule(() -> reloadWorld(() -> world.loadMap(target, target.applyRules(state.rules.mode()))), 10f);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.nominate.map.failed", target.name());
    }
}
