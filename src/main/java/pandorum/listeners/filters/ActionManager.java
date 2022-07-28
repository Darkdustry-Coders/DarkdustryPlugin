package pandorum.listeners.filters;

import mindustry.net.Administration.ActionFilter;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import pandorum.features.History;
import pandorum.features.history.entry.RotateEntry;

public class ActionManager implements ActionFilter {

    public boolean allow(PlayerAction action) {
        if (History.enabled() && action.type == ActionType.rotate) {
            var entry = new RotateEntry(action);
            History.putTileHistory(entry, action.tile);
        }

        return true;
    }
}
