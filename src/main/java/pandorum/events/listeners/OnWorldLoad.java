package pandorum.events.listeners;

import arc.func.Cons;
import arc.util.Time;
import mindustry.game.EventType.WorldLoadEvent;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.sendToChat;

public class OnWorldLoad implements Cons<WorldLoadEvent> {

    public void get(WorldLoadEvent event) {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        mapRateVotes.clear();

        history.clear();

        mapPlayTime = 0;
        canVote = true;

        Time.run(600f, () -> sendToChat("events.world-loaded"));
    }
}
