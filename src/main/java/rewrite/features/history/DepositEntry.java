package rewrite.features.history;

import arc.util.Time;
import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import rewrite.components.Icons;
import rewrite.utils.Find;

import static mindustry.Vars.content;
import static rewrite.components.Bundle.format;
import static rewrite.utils.Utils.formatDate;

public class DepositEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long time;

    public DepositEntry(DepositEvent event) {
        this.name = event.player.name;
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return format("history.deposit", Find.locale(player.locale), name, amount, Icons.get(content.item(itemID).name), Icons.get(content.block(blockID).name), formatDate(time));
    }
}
