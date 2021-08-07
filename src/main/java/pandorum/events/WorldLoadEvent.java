package pandorum.events;

import mindustry.game.EventType;
import mindustry.Vars;

import pandorum.PandorumPlugin;
import pandorum.struct.*;
import pandorum.comp.Config.PluginType;

public class WorldLoadEvent {
    public static void call(final EventType.WorldLoadEvent event) {
        if(PandorumPlugin.config.type == PluginType.sand) PandorumPlugin.timer.clear();
        PandorumPlugin.history = new CacheSeq[Vars.world.width()][Vars.world.height()];

        for(Tile tile : world.tiles){
            history[tile.x][tile.y] = Seqs.newBuilder()
                    .maximumSize(PandorumPlugin.config.historyLimit)
                    .expireAfterWrite(Duration.ofMillis(PandorumPlugin.config.expireDelay))
                    .build();
        }
    }
}
