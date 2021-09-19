package pandorum.events;

import mindustry.gen.Building;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;

import pandorum.struct.*;
import pandorum.entry.*;
import pandorum.PandorumPlugin;
import pandorum.Misc;

public class ActionFilter {
    public static boolean call(final PlayerAction action) {
        HistoryEntry entry;
        CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];

        switch(action.type) {
            case ActionType.rotate -> {
                entry = new RotateEntry(Misc.colorizedName(action.player), action.tile.build.block, action.rotation);
                entries.add(entry);
            }                
            case ActionType.withdrawItem -> {
                entry = new WithdrawEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
                entries.add(entry);
            }
            case ActionType.depositItem -> {
                entry = new DepositEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
                entries.add(entry);
            }
        }
        return true;
    }
}
