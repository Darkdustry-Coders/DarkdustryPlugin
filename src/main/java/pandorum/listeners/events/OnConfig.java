package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.ConfigEvent;
import pandorum.features.history.entry.ConfigEntry;
import pandorum.features.history.entry.HistoryEntry;

import static mindustry.Vars.world;
import static pandorum.PluginVars.history;
import static pandorum.PluginVars.historyEnabled;

public class OnConfig implements Cons<ConfigEvent> {

    public void get(ConfigEvent event) {
        if (historyEnabled() && event.player != null && event.tile.tileX() <= world.width() && event.tile.tileX() <= world.height()) {
            HistoryEntry entry = new ConfigEntry(event);
            history.putLinkedTiles(event.tile.tile, entry);
        }
    }
}
