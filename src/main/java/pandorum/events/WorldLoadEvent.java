package pandorum.events;

import mindustry.game.EventType;
import mindustry.Vars;
import mindustry.world.Tile;

import pandorum.PandorumPlugin;
import pandorum.struct.*;
import pandorum.comp.Config.PluginType;

import java.time.Duration;

public class WorldLoadEvent {
    public static void call(final EventType.WorldLoadEvent event) {
        if(PandorumPlugin.config.type == PluginType.sand) PandorumPlugin.timer.clear();
        PandorumPlugin.history = new CacheSeq[Vars.world.width()][Vars.world.height()];

        for(Tile tile : Vars.world.tiles){
            PandorumPlugin.history[tile.x][tile.y] = Seqs.newBuilder()
                    .maximumSize(PandorumPlugin.config.historyLimit)
                    .expireAfterWrite(Duration.ofMillis(PandorumPlugin.config.expireDelay))
                    .build();
        }
    }
}
