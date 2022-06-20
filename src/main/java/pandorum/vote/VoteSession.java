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

    protected int votes;

    public VoteSession() {
        this.task = start();
    }

    public abstract Task start();

    public abstract void stop();

    public abstract void vote(Player player, int sign);

    public abstract boolean checkPass();

    public int votesRequired() {
        return Mathf.ceil(voteRatio * Groups.player.size());
    }

    public Seq<String> voted() {
        return voted;
    }
}
