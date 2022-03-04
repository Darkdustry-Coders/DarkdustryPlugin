package pandorum.events.listeners;

import arc.struct.Seq;
import arc.util.Pack;
import mindustry.game.EventType.ConfigEvent;
import mindustry.world.blocks.power.PowerNode;
import pandorum.history.entry.ConfigEntry;
import pandorum.history.entry.HistoryEntry;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ConfigListener {

    public static void call(final ConfigEvent event) {
        if (config.historyEnabled() && event.player != null && event.tile.tileX() <= world.width() && event.tile.tileX() <= world.height()) {
            history.getAll(event.tile.tile.x, event.tile.tile.y, historyEntries -> {
                boolean connected = false;

                if (event.tile.block instanceof PowerNode node && historyEntries.any() && historyEntries.peek() instanceof ConfigEntry lastConfigEntry && lastConfigEntry.value instanceof Long value) {
                    int connections = event.tile.power.links.size;

                    connected = historyEntries.size >= 2
                            && historyEntries.get(historyEntries.size - 2) instanceof ConfigEntry configEntry
                            && configEntry.value instanceof Long longValue
                            && Pack.leftInt(longValue) != connections
                            && content.block(configEntry.blockID) instanceof PowerNode ? isLastUniqueCount(historyEntries, value, node.maxNodes) : connections > Pack.leftInt(value);
                }

                HistoryEntry entry = new ConfigEntry(event, connected);
                history.putLinkedTiles(event.tile.tile, entry);
            });
        }
    }

    private static boolean isLastUniqueCount(Seq<HistoryEntry> entries, long lastCount, int maxSearchBound) {
        for (int i = entries.size - 2; i >= maxSearchBound; i--) {
            if (entries.get(i) instanceof ConfigEntry configEntry && configEntry.value instanceof Long value) {
                if (Pack.leftInt(lastCount) > Pack.leftInt(value)) return true;
            }
        }

        return false;
    }
}
