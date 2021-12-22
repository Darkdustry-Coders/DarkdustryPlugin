package pandorum.events;

import arc.struct.Seq;
import mindustry.game.EventType;
import pandorum.PandorumPlugin;
import pandorum.entry.HistoryEntry;
import pandorum.entry.WithdrawEntry;

public class WithdrawListener {

    public static void call(final EventType.WithdrawEvent event) {
        if (PandorumPlugin.config.historyEnabled()) {
            HistoryEntry entry = new WithdrawEntry(event);
            event.tile.tile.getLinkedTiles(new Seq<>()).each(tile -> PandorumPlugin.history[tile.x][tile.y].add(entry));
        }
    }
}
