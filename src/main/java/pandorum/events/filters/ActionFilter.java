package pandorum.events.filters;

import arc.struct.Seq;
import mindustry.net.Administration;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Tile;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;

public class ActionFilter {
    public static boolean filter(final PlayerAction action) {
        if (PandorumPlugin.config.mode == Config.Gamemode.hexed || PandorumPlugin.config.mode == Config.Gamemode.hub || PandorumPlugin.config.mode == Config.Gamemode.castle) return true;
        if (action.type == Administration.ActionType.rotate) {
            HistoryEntry entry = new RotateEntry(action.player.coloredName(), action.tile.build.block, action.rotation);
            Seq<Tile> linkedTiles = action.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTiles) {
                PandorumPlugin.history[tile.x][tile.y].add(entry);
            }
        }
        return true;
    }
}
