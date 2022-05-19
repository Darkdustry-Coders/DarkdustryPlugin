package pandorum.vote;

import arc.Core;
import arc.files.Fi;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Player;
import mindustry.io.SaveIO;

import static pandorum.PluginVars.voteDuration;
import static pandorum.util.Utils.sendToChat;

public class VoteSaveSession extends VoteSession {

    protected final Fi target;

    public VoteSaveSession(VoteSession[] voteSession, Fi target) {
        super(voteSession);
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
    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        sendToChat("commands.nominate.save.vote", player.coloredName(), target.nameWithoutExtension(), votes, votesRequired());
        checkPass();
    }

    @Override
    public boolean checkPass() {
        if (votes >= votesRequired()) {
            sendToChat("commands.nominate.save.passed", target.nameWithoutExtension());
            stop();
            Core.app.post(() -> SaveIO.save(target));
            return true;
        }
        return false;
    }
}
