package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.ai.types.FormationAI;
import mindustry.game.EventType.BlockBuildEndEvent;
import pandorum.features.antigrief.history.entry.BlockEntry;
import pandorum.features.antigrief.history.entry.HistoryEntry;
import pandorum.database.models.PlayerModel;

import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class OnBlockBuildEnd implements Cons<BlockBuildEndEvent> {

    public void get(BlockBuildEndEvent event) {
        if (config.historyEnabled() && event.unit.isPlayer()) {
            HistoryEntry entry = new BlockEntry(event);
            event.unit.getControllerName();
            history.putLinkedTiles(event.tile, entry);
        }

        PlayerModel.find(event.unit.getPlayer(), playerModel -> {
            if (event.breaking) playerModel.buildingsDeconstructed++;
            else playerModel.buildingsBuilt++;
            playerModel.save();
        });
    }
}