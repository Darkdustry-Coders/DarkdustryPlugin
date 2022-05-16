package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.data.PlayerData;
import pandorum.features.antigrief.history.entry.BlockEntry;
import pandorum.features.antigrief.history.entry.HistoryEntry;

import static pandorum.PluginVars.*;

public class OnBlockBuildEnd implements Cons<BlockBuildEndEvent> {

    public void get(BlockBuildEndEvent event) {
        if (config.historyEnabled() && event.unit.isPlayer()) {
            HistoryEntry entry = new BlockEntry(event);
            history.putLinkedTiles(event.tile, entry);
        }

        if (!event.unit.isPlayer() || event.breaking) return;
        PlayerData data = datas.get(event.unit.getPlayer().uuid());
        data.buildingsBuilt++;
    }
}
