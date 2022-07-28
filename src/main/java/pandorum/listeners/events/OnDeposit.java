package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.DepositEvent;
import pandorum.features.Alerts;
import pandorum.features.History;
import pandorum.features.history.entry.DepositEntry;

public class OnDeposit implements Cons<DepositEvent> {

    public void get(DepositEvent event) {
        if (History.enabled() && event.player != null) {
            var entry = new DepositEntry(event);
            History.putTileHistory(entry, event.tile.tile);
        }

        Alerts.depositAlert(event);
    }
}
