package pandorum.events.filters;

import mindustry.net.Administration.ActionFilter;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.antigrief.history.entry.HistoryEntry;
import pandorum.antigrief.history.entry.RotateEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ActionManager implements ActionFilter {

    public boolean allow(final PlayerAction action) {
        if (config.historyEnabled() && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            history.putLinkedTiles(action.tile, entry);
        }

        return true;
    }
}
