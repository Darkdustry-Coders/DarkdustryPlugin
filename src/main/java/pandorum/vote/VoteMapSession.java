package pandorum.vote;

import arc.util.Log;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.sendToChat;

public class VoteMapSession extends VoteSession {

    protected final Map target;

    public VoteMapSession(Map target) {
        this.target = target;
    }

    @Override
    public Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.nominate.map.failed", target.name());
                stop();
            }
        }, voteDuration);
    }

    @Override
    public void stop() {
        voted.clear();
        task.cancel();
        currentVote = null;
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.map.vote", player.name, target.name(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.map.passed", target.name());

            Runnable r = () -> {
                WorldReloader reloader = new WorldReloader();

                reloader.begin();
                world.loadMap(target, target.applyRules(state.rules.mode()));

                state.rules = state.map.applyRules(state.rules.mode());
                logic.play();

                reloader.end();
            };

            Timer.schedule(() -> {
                try {
                    r.run();
                } catch (MapException e) {
                    Log.err("@: @", e.map.name(), e.getMessage());
                    net.closeServer();
                }
            }, 10f);
            stop();
            return true;
        }
        return false;
    }
}
