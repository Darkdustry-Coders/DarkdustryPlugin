package pandorum.events.filters;

import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ActionFilter {

    public static boolean filter(final PlayerAction action) {
        if (config.historyEnabled() && action.player != null && action.type == ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action);
            action.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
        }
        return true;
    }
}
