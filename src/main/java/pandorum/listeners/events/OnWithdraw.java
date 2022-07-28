package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WithdrawEvent;
import pandorum.features.History;
import pandorum.features.history.entry.WithdrawEntry;

public class OnWithdraw implements Cons<WithdrawEvent> {

    public void get(WithdrawEvent event) {
        if (History.enabled() && event.player != null) {
            var entry = new WithdrawEntry(event);
            History.putTileHistory(entry, event.tile.tile);
        }
    }
}
