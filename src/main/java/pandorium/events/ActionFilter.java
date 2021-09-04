package pandorium.events;

import mindustry.gen.Building;
import mindustry.net.Administration;

import pandorium.struct.*;
import pandorium.entry.*;
import pandorium.PandorumPlugin;
import pandorium.Misc;

public class ActionFilter {
    public static boolean call(final Administration.PlayerAction action) {
        if(action.type == Administration.ActionType.rotate){
            Building building = action.tile.build;
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];
            HistoryEntry entry = new RotateEntry(Misc.colorizedName(action.player), building.block, action.rotation);
            entries.add(entry);
        }
        return true;
    }
}