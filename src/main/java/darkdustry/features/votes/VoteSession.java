package darkdustry.features.votes;

import arc.math.Mathf;
import arc.struct.ObjectIntMap;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static darkdustry.PluginVars.*;

public abstract class VoteSession {

    /** Список uuid проголосовавших игроков и их голос. */
    public final ObjectIntMap<String> voted = new ObjectIntMap<>();
    /** Задача на завершение голосования. */
    public final Task end;

    public VoteSession() {
        end = Timer.schedule(this::fail, voteDuration);
    }

    public void vote(Player player, int sign) {
        voted.put(player.uuid(), sign);
        if (votes() >= votesRequired()) success();
    }

    public void left(Player player) {
        voted.remove(player.uuid());
        if (votes() >= votesRequired()) success();
    }

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
        return Mathf.ceil(Groups.player.size() * voteRatio);
    }
}
