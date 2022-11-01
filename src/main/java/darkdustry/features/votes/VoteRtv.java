package darkdustry.features.votes;

import arc.util.Timer;
import mindustry.gen.Player;
import mindustry.maps.Map;

import static darkdustry.PluginVars.mapLoadDelay;
import static darkdustry.utils.Utils.reloadWorld;
import static mindustry.Vars.*;
import static useful.Bundle.sendToChat;

public class VoteRtv extends VoteSession {

    public final Map target;

    public VoteRtv(Map target) {
        this.target = target;
    }

    @Override
    public void vote(Player player, int sign) {
        super.vote(player, sign);
        sendToChat("commands.rtv.vote", player.coloredName(), target.name(), votes(), votesRequired());
    }

    @Override
    public void left(Player player) {
        if (voted.remove(player.id) != 0)
            sendToChat("commands.rtv.left", player.coloredName(), votes(), votesRequired());
    }

    @Override
    public void success() {
        stop();
        sendToChat("commands.rtv.passed", target.name(), mapLoadDelay);
        Timer.schedule(() -> reloadWorld(() -> world.loadMap(target, target.applyRules(state.rules.mode()))), mapLoadDelay);
    }

    @Override
    public void fail() {
        stop();
        sendToChat("commands.rtv.failed", target.name());
    }
}