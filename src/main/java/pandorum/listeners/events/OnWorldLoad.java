package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WorldLoadEvent;

import static pandorum.PluginVars.*;

public class OnWorldLoad implements Cons<WorldLoadEvent> {

    public void get(WorldLoadEvent event) {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        history.clear();

        mapPlayTime = 0;
        canVote = true;
    }
}
