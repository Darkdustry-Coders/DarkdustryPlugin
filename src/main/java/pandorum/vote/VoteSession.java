package pandorum.vote;

import static pandorum.PandorumPlugin.config;

import arc.struct.ObjectSet;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public abstract class VoteSession{
    protected ObjectSet<String> voted = new ObjectSet<>();
    protected VoteSession[] map;
    protected Task task;
    protected int votes;

    public VoteSession(VoteSession[] map){
        this.map = map;
        this.task = start();
    }

    public ObjectSet<String> voted(){
        return voted;
    }

    protected abstract Task start();

    public abstract void vote(Player player, int d);

    protected abstract boolean checkPass();

    protected int votesRequired(){
        return (int)Math.ceil(config.voteRatio * Groups.player.size());
    }
}
