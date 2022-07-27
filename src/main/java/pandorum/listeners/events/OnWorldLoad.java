package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WorldLoadEvent;
import pandorum.features.History;

import static pandorum.PluginVars.*;

public class OnWorldLoad implements Cons<WorldLoadEvent> {

    public void get(WorldLoadEvent event) {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        History.reload();

        mapPlayTime = 0;
        canVote = true;
    }
}
