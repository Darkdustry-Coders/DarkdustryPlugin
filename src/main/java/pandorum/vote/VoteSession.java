package pandorum.vote;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static pandorum.PluginVars.voteRatio;

public abstract class VoteSession {

    protected final Seq<String> voted = new Seq<>();
    protected final Task task;

    protected final VoteSession[] voteSession;
    protected int votes;

    public VoteSession(VoteSession[] voteSession) {
        this.voteSession = voteSession;
        this.task = start();
    }

    protected abstract Task start();

    public abstract void vote(Player player, int sign);

    protected abstract boolean checkPass();

    protected int votesRequired() {
        return Mathf.ceil(voteRatio * Groups.player.size());
    }

    public void stop() {
        voted.clear();
        task.cancel();
        voteSession[0] = null;
    }

    public Seq<String> voted() {
        return voted;
    }
}
