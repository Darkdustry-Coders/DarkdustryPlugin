package pandorum.vote;

import arc.struct.Seq;
import arc.util.Timer.Task;
import mindustry.gen.Player;

// TODO кринж какой-то. Сделать что-то подобное: https://github.com/ThePotatoChronicler/potato-mindustry-plugin/blob/b011e9c777e901d84aa90d94169d6302f09b12e6/src/potato/Voteskip.java#L9
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
