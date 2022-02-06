package pandorum.events.listeners;

import mindustry.game.EventType.WithdrawEvent;
import pandorum.entry.CacheEntry;
import pandorum.entry.WithdrawEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class WithdrawListener {

    public static void call(final WithdrawEvent event) {
        if (config.historyEnabled()) {
            CacheEntry entry = new WithdrawEntry(event);
            event.tile.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }
    }
}
