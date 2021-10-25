package pandorum.events;

import arc.struct.Seq;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.entry.BlockEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

public class BlockBuildEndListener {
    public static void call(final EventType.BlockBuildEndEvent event) {
        if (PandorumPlugin.config.mode != Config.Gamemode.hexed && PandorumPlugin.config.mode != Config.Gamemode.hub && PandorumPlugin.config.mode != Config.Gamemode.castle) {
            HistoryEntry entry = new BlockEntry(event);

            Seq<Tile> linkedTiles = event.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }

        if (event.unit.isPlayer()) {
            PlayerModel.find(new BasicDBObject("UUID", event.unit.getPlayer().uuid()), playerInfo -> {
                if (event.breaking) playerInfo.buildingsDeconstructed++;
                else playerInfo.buildingsBuilt++;
                playerInfo.save();
            });
        }
    }
}