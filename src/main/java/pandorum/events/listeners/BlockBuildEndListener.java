package pandorum.events.listeners;

import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.database.models.PlayerModel;
import pandorum.history.entry.BlockEntry;
import pandorum.history.entry.HistoryEntry;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class BlockBuildEndListener {

    public static void call(final BlockBuildEndEvent event) {
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