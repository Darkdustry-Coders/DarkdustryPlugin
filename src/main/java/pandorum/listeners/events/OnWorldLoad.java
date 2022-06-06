package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WorldLoadEvent;
import pandorum.features.history.HistorySeq;

import static mindustry.Vars.world;
import static pandorum.PluginVars.*;

public class OnWorldLoad implements Cons<WorldLoadEvent> {

    public void get(WorldLoadEvent event) {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        history = new HistorySeq[world.width()][world.height()];
        world.tiles.eachTile(tile -> history[tile.x][tile.y] = new HistorySeq(maxTileHistoryCapacity));

        mapPlayTime = 0;
        canVote = true;
    }
}
