package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.data.PlayerData;
import pandorum.features.history.entry.BlockEntry;
import pandorum.features.history.entry.HistoryEntry;

import static pandorum.PluginVars.history;
import static pandorum.PluginVars.historyEnabled;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;

public class OnBlockBuildEnd implements Cons<BlockBuildEndEvent> {

    public void get(BlockBuildEndEvent event) {
        if (historyEnabled() && event.unit.isPlayer()) {
            HistoryEntry entry = new BlockEntry(event);
            history.putLinkedTiles(event.tile, entry);
        }

        if (!event.unit.isPlayer() || event.breaking) return;

        PlayerData data = getPlayerData(event.unit.getPlayer().uuid());
        data.buildingsBuilt++;
        setPlayerData(event.unit.getPlayer().uuid(), data);
    }
}
