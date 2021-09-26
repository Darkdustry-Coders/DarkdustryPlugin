package pandorum.events;

import arc.struct.*;
import mindustry.game.EventType;
import mindustry.world.Tile;

import pandorum.entry.*;
import pandorum.PandorumPlugin;

public class BlockBuildEndListener {
    public static void call(final EventType.BlockBuildEndEvent event) {
        HistoryEntry entry = new BlockEntry(event);

        Seq<Tile> linkedTile = event.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTile) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}