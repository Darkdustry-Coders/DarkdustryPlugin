package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer.Task;
import mindustry.gen.Player;

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

    public abstract int votesRequired();

    public Seq<String> voted() {
        return voted;
    }
}
