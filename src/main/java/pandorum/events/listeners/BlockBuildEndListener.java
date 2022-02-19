package pandorum.events.listeners;

import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.history.entry.BlockEntry;
import pandorum.history.entry.HistoryEntry;

import static pandorum.PluginVars.*;

public class BlockBuildEndListener {

    public static void call(final BlockBuildEndEvent event) {
        if (config.historyEnabled()) {
            HistoryEntry entry = new BlockEntry(event);
            event.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }

        playersInfo.find(event.unit.getPlayer(), playerModel -> {
            if (event.breaking) playerModel.buildingsDeconstructed++;
            else playerModel.buildingsBuilt++;
            playerModel.save();
        });
    }
}