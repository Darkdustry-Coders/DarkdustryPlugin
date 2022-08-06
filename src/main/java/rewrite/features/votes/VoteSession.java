package rewrite.features.votes;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static rewrite.PluginVars.*;

public abstract class VoteSession {

    /** Список uuid проголосовавших игроков. */
    public final Seq<String> voted = new Seq<>();
    /** Задача на завершение голосования. */
    public final Task end;
    /** Общий счёт голосов. */
    public int votes;

    public VoteSession() {
        end = Timer.schedule(this::fail, voteDuration);
    }

    public void vote(Player player, int sign) {
        votes += sign;
        voted.add(player.uuid());
        if (votes >= votesRequired()) success();
    }

    public abstract void success();

    public abstract void fail();

    public void stop() {
        vote = null;
        end.cancel();
    }

    public int votesRequired() {
        return Mathf.ceil(Groups.player.size() * voteRatio);
    }
}
