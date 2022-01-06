package pandorum.vote;

import arc.util.Log;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.config;

public class VoteMapSession extends VoteSession {
    protected final Map target;

    public VoteMapSession(VoteSession[] session, Map target) {
        super(session);
        this.target = target;
    }

    @Override
    protected Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.nominate.map.failed", target.name());
                stop();
            }
        }, config.voteDuration);
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.map.vote", player.coloredName(), target.name(), votes, votesRequired());
        checkPass();
    }

    @Override
    protected boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.map.passed", target.name());
            stop();

            Runnable r = () -> {
                WorldReloader reloader = new WorldReloader();

                reloader.begin();

                world.loadMap(target, target.applyRules(state.rules.mode()));

                state.rules = state.map.applyRules(state.rules.mode());
                logic.play();

                reloader.end();
            };

            Timer.schedule(new Task() {
                @Override
                public void run() {
                    try {
                        r.run();
                    } catch (MapException e) {
                        Log.err("@: @", e.map.name(), e.getMessage());
                        net.closeServer();
                    }
                }
            }, 10f);
            return true;
        }
        return false;
    }
}
