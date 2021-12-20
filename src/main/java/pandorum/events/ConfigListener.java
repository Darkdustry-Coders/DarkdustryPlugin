package pandorum.events;

import arc.struct.Seq;
import arc.util.Pack;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerNode;
import pandorum.PandorumPlugin;
import pandorum.entry.ConfigEntry;
import pandorum.entry.HistoryEntry;
import pandorum.struct.CacheSeq;

import static mindustry.Vars.content;

public class ConfigListener {

    public static void call(final EventType.ConfigEvent event) {
        if (PandorumPlugin.config.historyEnabled() && event.player != null && event.tile.tileX() <= Vars.world.width() && event.tile.tileX() <= Vars.world.height()) {
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[event.tile.tileX()][event.tile.tileY()];
            int connections = event.tile.power != null ? event.tile.power.links.size : 0;
            boolean connect = connections > 0;

            if (!entries.isEmpty() && entries.peek() instanceof ConfigEntry lastConfigEntry && lastConfigEntry.value instanceof Long value) {
                boolean isBatch = entries.size >= 2 && entries.get(entries.size - 2) instanceof ConfigEntry configEntry && configEntry.value instanceof Long l && Pack.leftInt(l) != connections && content.block(configEntry.blockID) instanceof PowerNode;

                connect = event.tile.block instanceof PowerNode node ? isBatch ? isLastUniqueCount(entries, value, node.maxNodes) : connections > Pack.leftInt(value) : event.value instanceof Integer i && i >= 0;
            }

            HistoryEntry entry = new ConfigEntry(event, connect);

            Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }
    }

    private static boolean isLastUniqueCount(CacheSeq<HistoryEntry> entries, long lastCount, int maxSearchBound) {
        for (int i = entries.size - 2; i >= maxSearchBound; i--) {
            if (entries.get(i) instanceof ConfigEntry configEntry && configEntry.value instanceof Long value) {
                if (Pack.leftInt(lastCount) > Pack.leftInt(value)) return true;
            }
        }
        return false;
    }
}
