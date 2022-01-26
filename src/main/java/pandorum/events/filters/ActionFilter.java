package pandorum.events.filters;

import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.entry.CacheEntry;
import pandorum.entry.RotateEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class ActionFilter {

    public static boolean filter(final PlayerAction action) {
        if (config.historyEnabled() && action.player != null && action.type == ActionType.rotate) {
            CacheEntry entry = new RotateEntry(action);
            action.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }

        return true;
    }
}
