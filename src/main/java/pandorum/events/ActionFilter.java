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
        HistoryEntry entry = switch(action.type) {
            case ActionType.rotate -> new RotateEntry(Misc.colorizedName(action.player), action.tile.build.block, action.rotation);
            case ActionType.withdrawItem -> new WithdrawEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
            case ActionType.depositItem -> new DepositEntry(Misc.colorizedName(action.player), action.tile.build.block, action.item, action.itemAmount);
            default -> null;
        };

        if (entry != null) {
            CacheSeq<HistoryEntry> entries = PandorumPlugin.history[action.tile.x][action.tile.y];
            entries.add(entry);
        }
        return true;
    }
}
