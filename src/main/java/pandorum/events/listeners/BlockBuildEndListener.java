package pandorum.events.listeners;

import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.entry.BlockEntry;
import pandorum.entry.CacheEntry;
import pandorum.models.PlayerModel;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class BlockBuildEndListener {

    public static void call(final BlockBuildEndEvent event) {
        if (config.historyEnabled()) {
            CacheEntry entry = new BlockEntry(event);
            event.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }

        PlayerModel.find(event.unit.getPlayer(), playerModel -> {
            if (event.breaking) playerModel.buildingsDeconstructed++;
            else playerModel.buildingsBuilt++;
            playerModel.save();
        });
    }
}