package pandorum.events;

import arc.struct.Seq;
import com.mongodb.BasicDBObject;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.Gamemode;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class DepositListener {
    public static void call(final EventType.DepositEvent event) {
        if (PandorumPlugin.config.mode == Gamemode.hexed || PandorumPlugin.config.mode == Gamemode.hub || PandorumPlugin.config.mode == Gamemode.castle) return;

        if (event.tile.block() == Blocks.thoriumReactor && event.item == Items.thorium && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                if (playerInfo.alerts) bundled(p, "events.withdraw-thorium", event.player.coloredName(), event.tile.tileX(), event.tile.tileY());
            }));
        }

        HistoryEntry entry = new DepositEntry(event);
        Seq<Tile> linkedTiles = event.tile.tile.getLinkedTiles(new Seq<>());
        for (Tile tile : linkedTiles) {
            PandorumPlugin.history[tile.x][tile.y].add(entry);
        }
    }
}
