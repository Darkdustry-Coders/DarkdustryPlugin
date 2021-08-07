package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.content.Blocks;
import mindustry.world.Tile;

import pandorum.entry.*;
import pandorum.PandorumPlugin;

public class BlockBuildEndEvent {
    public static void call(final EventType.BlockBuildEndEvent event) {
        HistoryEntry historyEntry = new BlockEntry(event);

        Seq<Tile> linkedTile = event.tile.getLinkedTiles(new Seq<>());
        for(Tile tile : linkedTile){
            PandorumPlugin.history[tile.x][tile.y].add(historyEntry);
        }
    }
}
