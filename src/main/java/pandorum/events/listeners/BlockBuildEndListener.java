package pandorum.events.listeners;

import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.entry.BlockEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class BlockBuildEndListener {

    public static void call(final BlockBuildEndEvent event) {
        if (config.historyEnabled()) {
            HistoryEntry entry = new BlockEntry(event);
            event.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
        }

        if (event.unit.isPlayer()) {
            PlayerModel.find(event.unit.getPlayer().uuid(), playerModel -> {
                if (event.breaking) playerModel.buildingsDeconstructed++;
                else playerModel.buildingsBuilt++;
                playerModel.save();
            });
        }
    }
}