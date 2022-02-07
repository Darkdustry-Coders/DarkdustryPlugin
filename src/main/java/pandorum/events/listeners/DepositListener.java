package pandorum.events.listeners;

import mindustry.game.EventType.DepositEvent;
import mindustry.world.blocks.power.NuclearReactor;
import pandorum.comp.Icons;
import pandorum.entry.CacheEntry;
import pandorum.entry.DepositEntry;
import pandorum.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;

public class DepositListener {

    public static void call(final DepositEvent event) {
        if (config.alertsEnabled() && event.tile.block instanceof NuclearReactor && event.item.explosiveness > 0f && event.player.team().cores().contains(c -> event.tile.dst(c) < alertsDistance)) {
            Utils.eachPlayerInTeam(event.player.team(), player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) bundled(player, "events.withdraw-thorium", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
            }));
        }

        if (config.historyEnabled()) {
            CacheEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(tile -> history.put(tile.x, tile.y, entry));
        }
    }
}
