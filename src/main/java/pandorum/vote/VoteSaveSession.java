package pandorum.vote;

import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.sendToChat;

public class VoteSaveSession extends VoteSession {

    protected final Fi target;

    public VoteSaveSession(Fi target) {
        super();
        this.target = target;
    }

    @Override
    public Task start() {
        return Timer.schedule(() -> {
            if (!checkPass()) {
                sendToChat("commands.nominate.save.failed", target.nameWithoutExtension());
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
        sendToChat("commands.nominate.save.vote", player.name, target.nameWithoutExtension(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.save.passed", target.nameWithoutExtension());
            Core.app.post(() -> SaveIO.save(target));
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
