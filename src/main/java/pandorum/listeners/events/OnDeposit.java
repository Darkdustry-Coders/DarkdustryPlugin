package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.DepositEvent;
import pandorum.components.Icons;
import pandorum.data.PlayerData;
import pandorum.features.Alerts;
import pandorum.features.history.entry.DepositEntry;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.util.PlayerUtils;
import pandorum.util.Utils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.alertsEnabled;
import static pandorum.PluginVars.historyEnabled;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.PlayerUtils.bundled;

public class OnDeposit implements Cons<DepositEvent> {

    public void get(DepositEvent event) {
        if (historyEnabled()) {
            HistoryEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(tile -> Utils.getHistory(tile.x, tile.y).add(entry));
        }

        if (!alertsEnabled() || !state.rules.reactorExplosions) return;

        if (Alerts.isDangerousDeposit(event.tile, event.tile.team, event.item)) {
            event.player.team().data().players.each(player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alert.dangerous-deposit", event.player.name, Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            });
        }
    }
}
