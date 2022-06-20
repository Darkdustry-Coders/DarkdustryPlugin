package pandorum.vote;

import arc.files.Fi;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;

import static mindustry.Vars.*;
import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.sendToChat;

public class VoteLoadSession extends VoteSession {

    protected final Fi target;

    public VoteLoadSession(Fi target) {
        super();
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
    public void stop() {
        voted.clear();
        task.cancel();
        currentVote = null;
    }

    @Override
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.load.vote", player.name, target.nameWithoutExtension(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.load.passed", target.nameWithoutExtension());

            Timer.schedule(() -> {
                try {
                    WorldReloader reloader = new WorldReloader();

                    reloader.begin();
                    SaveIO.load(target);

                    state.rules = state.map.applyRules(state.rules.mode());
                    logic.play();

                    reloader.end();
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

    @Override
    public int votesRequired() {
        return Mathf.ceil(voteRatio * Groups.player.size());
    }
}
