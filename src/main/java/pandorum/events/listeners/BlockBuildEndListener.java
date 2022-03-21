package pandorum.events.listeners;

import arc.func.Cons;
import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.antigrief.history.entry.BlockEntry;
import pandorum.antigrief.history.entry.HistoryEntry;
import pandorum.database.models.PlayerModel;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class BlockBuildEndListener implements Cons<BlockBuildEndEvent> {

    public void get(BlockBuildEndEvent event) {
        if (config.historyEnabled()) {
            HistoryEntry entry = new BlockEntry(event);
            history.putLinkedTiles(event.tile, entry);
        }

        PlayerModel.find(event.unit.getPlayer(), playerModel -> {
            if (event.breaking) playerModel.buildingsDeconstructed++;
            else playerModel.buildingsBuilt++;
            playerModel.save();
        });
    }
}