package pandorum.events;

import arc.struct.Seq;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.world.Tile;

import org.bson.Document;
import pandorum.comp.Config;
import pandorum.entry.*;
import pandorum.PandorumPlugin;

public class BlockBuildEndListener {
    public static void call(final EventType.BlockBuildEndEvent event) {
        if (PandorumPlugin.config.type != Config.PluginType.other) {
            HistoryEntry entry = new BlockEntry(event);

            Seq<Tile> linkedTiles = event.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }

        if (event.unit.isPlayer()) {
            Player player = event.unit.getPlayer();
            Document playerInfo = PandorumPlugin.createInfo(player);
            long buildingsBuilt = playerInfo.getLong("buildings") + 1;
            playerInfo.replace("buildings", buildingsBuilt);
            PandorumPlugin.savePlayerStats(player.uuid());
        }
    }
}