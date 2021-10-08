package pandorum.events;

import arc.struct.Seq;
import mindustry.net.Administration;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Tile;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;

public class ActionFilter {
    public static boolean call(final PlayerAction action) {
        if (PandorumPlugin.config.type != Config.PluginType.other && action.type == Administration.ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(Misc.colorizedName(action.player), action.tile.build.block, action.rotation);
            Seq<Tile> linkedTiles = action.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }
        return true;
    }
}
