package pandorium.events;

import arc.struct.*;
import mindustry.game.EventType;
import mindustry.world.Tile;

import pandorium.entry.*;
import pandorium.PandorumPlugin;

public class BlockBuildEndEvent {
    public static void call(final EventType.BlockBuildEndEvent event) {
        HistoryEntry historyEntry = new BlockEntry(event);

        Seq<Tile> linkedTile = event.tile.getLinkedTiles(new Seq<>());
        for(Tile tile : linkedTile){
            PandorumPlugin.history[tile.x][tile.y].add(historyEntry);
        }
    }
}