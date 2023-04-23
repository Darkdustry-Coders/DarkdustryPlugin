package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.game.EventType.WithdrawEvent;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.utils.Utils.formatShortDate;
import static mindustry.Vars.content;
import static mindustry.Vars.netServer;

public class WithdrawEntry implements HistoryEntry {

    public final String uuid;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long time;

    public WithdrawEntry(WithdrawEvent event) {
        this.uuid = event.player.uuid();
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        return Bundle.format("history.withdraw", player, info.lastName, amount, Icons.icon(content.item(itemID)), Icons.icon(content.block(blockID)), formatShortDate(time));
    }
}