package pandorum.events;

import mindustry.net.Administration.PlayerAction;
import pandorum.Misc;
import pandorum.PandorumPlugin;
import pandorum.entry.DepositEntry;
import pandorum.entry.HistoryEntry;
import pandorum.entry.RotateEntry;
import pandorum.entry.WithdrawEntry;
import pandorum.struct.CacheSeq;

public class ActionFilter {
    public static boolean call(final PlayerAction action) {
        HistoryEntry entry;
        CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];

        switch(action.type) {
            case rotate -> {
                entry = new RotateEntry(Misc.colorizedName(action.player), action.tile.build.block, action.rotation);
                entries.add(entry);
            }                
            case withdrawItem -> {
                entry = new WithdrawEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
                entries.add(entry);
            }
            case depositItem -> {
                entry = new DepositEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
                entries.add(entry);
            }
        }
        return true;
    }
}
