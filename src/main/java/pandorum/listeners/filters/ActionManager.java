package pandorum.listeners.filters;

import mindustry.net.Administration.ActionFilter;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.features.History;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.features.history.entry.RotateEntry;
import pandorum.util.Utils;

public class ActionManager implements ActionFilter {

    public boolean allow(PlayerAction action) {
        if (History.enabled() && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            action.tile.getLinkedTiles(tile -> Utils.getHistory(tile.x, tile.y).add(entry));
        }

        return true;
    }
}
