package pandorum.events.listeners;

import arc.util.Pack;
import mindustry.game.EventType.ConfigEvent;
import mindustry.world.blocks.power.PowerNode;
import pandorum.entry.ConfigEntry;
import pandorum.entry.HistoryEntry;
import pandorum.struct.CacheSeq;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ConfigListener {

    public static void call(final ConfigEvent event) {
        if (config.historyEnabled() && event.player != null && event.tile.tileX() <= world.width() && event.tile.tileX() <= world.height()) {
            CacheSeq<HistoryEntry> entries = history[event.tile.tileX()][event.tile.tileY()];
            boolean connect = false;

            if (event.tile.block instanceof PowerNode node && entries.any() && entries.peek() instanceof ConfigEntry lastConfigEntry && lastConfigEntry.value instanceof Long value) {
                int connections = event.tile.power.links.size;
                connect = entries.size >= 2 && entries.get(entries.size - 2) instanceof ConfigEntry configEntry && configEntry.value instanceof Long longValue && Pack.leftInt(longValue) != connections && content.block(configEntry.blockID) instanceof PowerNode ? isLastUniqueCount(entries, value, node.maxNodes) : connections > Pack.leftInt(value);
            }

            HistoryEntry entry = new ConfigEntry(event, connect);
            event.tile.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
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
