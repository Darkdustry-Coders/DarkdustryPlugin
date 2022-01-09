package pandorum.events;

import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Groups;
import mindustry.world.blocks.power.NuclearReactor;
import pandorum.comp.Icons;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.*;

public class DepositListener {

    public static void call(final DepositEvent event) {
        if (config.alertsEnabled() && event.tile.block instanceof NuclearReactor && event.item.explosiveness > 0f && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < alertsDistance)) {
            Groups.player.each(p -> p.team() == event.player.team(), p -> PlayerModel.find(p.uuid(), playerInfo -> {
                if (playerInfo.alerts) bundled(p, "events.withdraw-thorium", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
            }));
        }

        if (config.historyEnabled() && event.player != null) {
            HistoryEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(tile -> history[tile.x][tile.y].add(entry));
        }
    }
}
