package pandorum.events;

import arc.struct.Seq;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Tile;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;
import pandorum.entry.WithdrawEntry;

public class ActionFilter {
    public static boolean call(final PlayerAction action) {
        HistoryEntry entry = switch(action.type) {
            case rotate -> new RotateEntry(Misc.colorizedName(action.player), action.tile.build.block, action.rotation);              
            case withdrawItem -> new WithdrawEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
            case depositItem -> new DepositEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
            default -> null;
        };
        if (entry != null || action.tile != null) {
            Seq<Tile> linkedTiles = action.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[action.tile.x][action.tile.y].add(entry);
            }
        }
        return true;
    }
}
