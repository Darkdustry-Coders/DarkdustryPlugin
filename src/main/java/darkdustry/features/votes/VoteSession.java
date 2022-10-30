package darkdustry.features.votes;

import arc.math.Mathf;
import arc.struct.IntIntMap;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.*;

import static darkdustry.PluginVars.*;

public abstract class VoteSession {

    /** Список id проголосовавших игроков и их голоса. */
    public final IntIntMap voted = new IntIntMap();

    /** Задача на завершение голосования. */
    public final Task end;

    public VoteSession() {
        end = Timer.schedule(this::fail, voteDuration);
    }

    public void vote(Player player, int sign) {
        voted.put(player.id, sign);
    }

    public abstract void left(Player player);

    public abstract void success();

    public abstract void fail();

    public void stop() {
        vote = null;
        end.cancel();
    }

    public int votes() {
        return voted.values().toArray().sum();
    }

    public int votesRequired() {
        return Groups.player.size() > 2 ? Mathf.ceil(Groups.player.size() * voteRatio) : Groups.player.size();
    }
}