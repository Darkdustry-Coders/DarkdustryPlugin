package pandorum.events;

import arc.struct.*;
import mindustry.game.EventType;
import mindustry.world.Tile;

import pandorum.comp.Config;
import pandorum.entry.*;
import pandorum.PandorumPlugin;

public class BlockBuildEndListener {
    public static void call(final EventType.BlockBuildEndEvent event) {
        if (PandorumPlugin.config.type == Config.PluginType.other) return;
        HistoryEntry entry = new BlockEntry(event);

        Seq<Tile> linkedTiles = event.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}