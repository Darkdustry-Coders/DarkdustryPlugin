package pandorum.events.listeners;

import arc.func.Cons;
import mindustry.game.EventType.DepositEvent;
import pandorum.antigrief.Alerts;
import pandorum.antigrief.history.entry.DepositEntry;
import pandorum.antigrief.history.entry.HistoryEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class DepositListener implements Cons<DepositEvent> {

    public void get(DepositEvent event) {
        Alerts.depositAlert(event);

        if (config.historyEnabled()) {
            HistoryEntry entry = new DepositEntry(event);
            history.putLinkedTiles(event.tile.tile, entry);
        }
    }
}
