package pandorum.events.listeners;

import mindustry.game.EventType.WithdrawEvent;
import pandorum.history.entry.HistoryEntry;
import pandorum.history.entry.WithdrawEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class WithdrawListener {

    public static void call(final WithdrawEvent event) {
        if (config.historyEnabled()) {
            HistoryEntry entry = new WithdrawEntry(event);
            history.putLinkedTiles(event.tile.tile, entry);
        }
    }
}
