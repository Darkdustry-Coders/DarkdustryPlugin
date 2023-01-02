package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.game.EventType.WithdrawEvent;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.utils.Utils.formatShortDate;
import static mindustry.Vars.content;

public class WithdrawEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long time;

    public WithdrawEntry(WithdrawEvent event) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return Bundle.format("history.withdraw", player, name, amount, Icons.getIcon(content.item(itemID)), Icons.getIcon(content.block(blockID)), formatShortDate(time));
    }
}