package pandorum.events.filters;

import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.PandorumPlugin;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;

public class ActionFilter {

    public static boolean filter(final PlayerAction action) {
        if (PandorumPlugin.config.historyEnabled() && action.player != null && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            action.tile.getLinkedTiles(tile -> PandorumPlugin.history[tile.x][tile.y].add(entry));
        }
        return true;
    }
}
