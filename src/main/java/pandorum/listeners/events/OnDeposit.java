package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.DepositEvent;
import pandorum.components.Icons;
import pandorum.data.PlayerData;
import pandorum.features.history.entry.DepositEntry;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.util.PlayerUtils;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Utils.isDangerousDeposit;

public class OnDeposit implements Cons<DepositEvent> {

    public void get(DepositEvent event) {
        if (historyEnabled()) {
            HistoryEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
        }

        if (!alertsEnabled() || !state.rules.reactorExplosions) return;

        if (isDangerousDeposit(event.tile, event.tile.team, event.item)) {
            PlayerUtils.eachPlayer(event.player.team(), player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alert.dangerous-deposit", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            });
        }
    }
}
