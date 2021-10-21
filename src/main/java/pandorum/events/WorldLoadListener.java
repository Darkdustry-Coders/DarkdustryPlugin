package pandorum.events;

import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

import java.time.Duration;

import static mindustry.Vars.world;

public class WorldLoadListener {
    @SuppressWarnings("unchecked")
    public static void call(final EventType.WorldLoadEvent event) {
        if (PandorumPlugin.config.mode != Config.Gamemode.hexed && PandorumPlugin.config.mode != Config.Gamemode.hub && PandorumPlugin.config.mode != Config.Gamemode.castle) {
            PandorumPlugin.history = new CacheSeq[world.width()][world.height()];

            for (Tile tile : world.tiles) {
                PandorumPlugin.history[tile.x][tile.y] = Seqs.newBuilder()
                        .maximumSize(PandorumPlugin.config.historyLimit)
                        .expireAfterWrite(Duration.ofMillis(PandorumPlugin.config.expireDelay))
                        .build();
            }
        }
    }
}
