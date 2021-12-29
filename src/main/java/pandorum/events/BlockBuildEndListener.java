package pandorum.events;

import mindustry.game.EventType;
import pandorum.PandorumPlugin;
import pandorum.entry.BlockEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

public class BlockBuildEndListener {

    public static void call(final EventType.BlockBuildEndEvent event) {
        if (PandorumPlugin.config.historyEnabled()) {
            HistoryEntry entry = new BlockEntry(event);
            event.tile.getLinkedTiles(tile -> PandorumPlugin.history[tile.x][tile.y].add(entry));
        }

        if (event.unit.isPlayer()) {
            PlayerModel.find(event.unit.getPlayer().uuid(), playerInfo -> {
                if (event.breaking) playerInfo.buildingsDeconstructed++;
                else playerInfo.buildingsBuilt++;
                playerInfo.save();
            });
        }
    }
}