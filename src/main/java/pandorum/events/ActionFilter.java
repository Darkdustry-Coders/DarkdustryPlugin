package pandorum.events;

import mindustry.gen.Building;
import mindustry.net.Administration;

import pandorum.struct.*;
import pandorum.entry.*;
import pandorum.PandorumPlugin;
import pandorum.Misc;

public class ActionFilter {
    public static boolean call(final Administration.PlayerAction action) {
        if (action.type == Administration.ActionType.rotate) {
            Building building = action.tile.build;
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];
            HistoryEntry entry = new RotateEntry(Misc.colorizedName(action.player), building.block, action.rotation);
            entries.add(entry);
        } else if (action.type == Administration.ActionType.withdrawItem) {
            Building building = action.tile.build;
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];
            HistoryEntry entry = new WithdrawEntry(Misc.colorizedName(action.player), building.block, action.item, action.itemAmount);
            entries.add(entry);
        } else if (action.type == Administration.ActionType.depositItem) {
            Building building = action.tile.build;
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];
            HistoryEntry entry = new DepositEntry(Misc.colorizedName(action.player), building.block, action.item, action.itemAmount);
            entries.add(entry);
        }
        return true;
    }
}
