package pandorum.events.listeners;

import arc.func.Cons;
import mindustry.game.EventType.WithdrawEvent;
import pandorum.antigrief.history.entry.HistoryEntry;
import pandorum.antigrief.history.entry.WithdrawEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class WithdrawListener implements Cons<WithdrawEvent> {

    public void get(WithdrawEvent event) {
        if (config.historyEnabled()) {
            HistoryEntry entry = new WithdrawEntry(event);
            history.putLinkedTiles(event.tile.tile, entry);
        }
    }
}
