package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import useful.Bundle;

import static darkdustry.utils.Utils.formatShortDate;
import static mindustry.Vars.content;

public class DepositEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long time;

    public DepositEntry(DepositEvent event) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return Bundle.format("history.deposit", player, name, amount, Icons.get(content.item(itemID)), Icons.get(content.block(blockID)), formatShortDate(time));
    }
}