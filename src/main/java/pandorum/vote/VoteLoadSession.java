package pandorum.vote;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import static pandorum.PluginVars.voteDuration;
import static pandorum.util.PlayerUtils.sendToChat;

public class VoteLoadSession extends VoteSession {

    protected final Fi target;

    public VoteLoadSession(VoteSession[] voteSession, Fi target) {
        super(voteSession);
        this.target = target;
    }

    @Override
    public Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.nominate.load.failed", target.nameWithoutExtension());
                stop();
            }
        }, voteDuration);
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.load.vote", player.coloredName(), target.nameWithoutExtension(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.load.passed", target.nameWithoutExtension());
            stop();

            Runnable r = () -> {
                WorldReloader reloader = new WorldReloader();

                reloader.begin();
                SaveIO.load(target);

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
