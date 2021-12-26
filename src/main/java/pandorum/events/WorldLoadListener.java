package pandorum.events;

import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.annotations.events.EventListener;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

import java.time.Duration;

import static mindustry.Vars.world;
import static pandorum.Misc.InitializeHistory;

public class WorldLoadListener {
    @EventListener(eventType = EventType.WorldLoadEvent.class)
    public static void call(final EventType.WorldLoadEvent event) {
        InitializeHistory();
    }
}
