package pandorum.events.listeners;

import mindustry.game.EventType.DepositEvent;
import pandorum.components.Icons;
import pandorum.database.models.PlayerModel;
import pandorum.history.entry.DepositEntry;
import pandorum.history.entry.HistoryEntry;
import pandorum.util.Utils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;

public class DepositListener {

    public static void call(final DepositEvent event) {
        if (config.alertsEnabled() && state.rules.reactorExplosions && dangerousDepositBlocks.containsKey(event.tile.block) && dangerousDepositBlocks.get(event.tile.block) == event.item && event.player.team().cores().contains(c -> event.tile.dst(c) < alertsDistance)) {
            Utils.eachPlayerInTeam(event.player.team(), player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) {
                    bundled(player, "events.withdraw-thorium", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            }));
        }

        if (config.historyEnabled()) {
            HistoryEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }
    }
}
