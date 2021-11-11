package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static pandorum.PandorumPlugin.config;

public abstract class VoteSession {
    protected Seq<String> voted = new Seq<>();
    protected VoteSession[] session;
    protected Task task;
    protected int votes;

    public VoteSession(VoteSession[] session) {
        this.session = session;
        this.task = start();
    }

    public Seq<String> voted() {
        return voted;
    }

    protected abstract Task start();

    public abstract void vote(Player player, int sign);

    protected abstract boolean checkPass();

    public int votesRequired() {
        return (int) Math.ceil(config.voteRatio * Groups.player.size());
    }

    public void stop() {
        voted.clear();
        session[0] = null;
        task.cancel();
    }
}
