package darkdustry.features.votes;

import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.maps.Map;

import static darkdustry.PluginVars.mapLoadTime;
import static darkdustry.components.Bundle.sendToChat;
import static darkdustry.utils.Utils.reloadWorld;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class VoteRtv extends VoteSession {

    public final Map target;

    public VoteRtv(Map target) {
        super();
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.rtv.vote", player.coloredName(), target.name(), votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        super.left(player);
        sendToChat("commands.rtv.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.rtv.passed", target.name(), mapLoadTime);
        Timer.schedule(() -> reloadWorld(() -> world.loadMap(target, target.applyRules(state.rules.mode()))), mapLoadTime);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.rtv.failed");
    }
}
