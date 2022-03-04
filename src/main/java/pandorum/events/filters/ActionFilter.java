package pandorum.events.filters;

import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.history.entry.HistoryEntry;
import pandorum.history.entry.RotateEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ActionFilter {

    public static boolean filter(final PlayerAction action) {
        if (config.historyEnabled() && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            history.putLinkedTiles(action.tile, entry);
        }

        return true;
    }
}
