package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.WithdrawEvent;
import pandorum.features.History;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.features.history.entry.WithdrawEntry;
import pandorum.util.Utils;

public class OnWithdraw implements Cons<WithdrawEvent> {

    public void get(WithdrawEvent event) {
        if (History.enabled()) {
            HistoryEntry entry = new WithdrawEntry(event);
            event.tile.tile.getLinkedTiles(tile -> Utils.getHistory(tile.x, tile.y).add(entry));
        }
    }
}
