package pandorum.events;

import arc.struct.Seq;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.world.blocks.power.NuclearReactor;
import pandorum.PandorumPlugin;
import pandorum.comp.Icons;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.Misc.bundled;

public class DepositListener {

    public static void call(final EventType.DepositEvent event) {
        if (state.rules.reactorExplosions && PandorumPlugin.config.alertsEnabled() && event.tile.block instanceof NuclearReactor && event.item.explosiveness > 0f && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertsDistance)) {
            Groups.player.each(p -> p.team() == event.player.team(), p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                if (playerInfo.alerts) {
                    bundled(p, "events.withdraw-thorium", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            }));
        }

        if (PandorumPlugin.config.historyEnabled()) {
            HistoryEntry entry = new DepositEntry(event);
            event.tile.tile.getLinkedTiles(new Seq<>()).each(tile -> PandorumPlugin.history[tile.x][tile.y].add(entry));
        }
    }
}
