package pandorum.listeners.filters;

import mindustry.net.Administration.ActionFilter;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.features.history.entry.RotateEntry;

import static pandorum.PluginVars.history;
import static pandorum.PluginVars.historyEnabled;

public class ActionManager implements ActionFilter {

    public boolean allow(PlayerAction action) {
        if (historyEnabled() && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            action.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
        }

        return true;
    }
}
