package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.features.History;
import pandorum.features.history.entry.BlockEntry;

import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;

public class OnBlockBuildEnd implements Cons<BlockBuildEndEvent> {

    public void get(BlockBuildEndEvent event) {
        if (History.enabled() && event.tile.build != null && event.unit.isPlayer()) {
            var entry = new BlockEntry(event);
            History.putTileHistory(entry, event.tile);
        }

        if (!event.unit.isPlayer() || event.breaking) return;

        var data = getPlayerData(event.unit.getPlayer().uuid());
        data.buildingsBuilt++;
        setPlayerData(event.unit.getPlayer().uuid(), data);
    }
}
