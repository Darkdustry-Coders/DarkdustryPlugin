package darkdustry.features.votes;

import arc.math.Mathf;
import arc.struct.ObjectIntMap;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.*;

import static darkdustry.PluginVars.*;

public abstract class VoteSession {

    public final ObjectIntMap<Player> votes = new ObjectIntMap<>();
    public final Task end;

    public VoteSession() {
        this.end = Timer.schedule(this::fail, voteDuration);
    }

    public void vote(Player player, int sign) {
        votes.put(player, sign);

        if (votes() >= votesRequired())
            success();
    }

    public abstract void left(Player player);

    public abstract void success();

    public abstract void fail();

    public void stop() {
        end.cancel();
        vote = null;
    }

    public int votes() {
        return votes.values().toArray().sum();
    }

    public int votesRequired() {
        return Math.max(Math.min(2, Groups.player.size()), Mathf.ceil(Groups.player.size() * voteRatio));
    }
}