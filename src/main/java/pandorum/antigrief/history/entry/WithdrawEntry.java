package pandorum.antigrief.history.entry;

import arc.util.Time;
import mindustry.game.EventType.WithdrawEvent;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import static mindustry.Vars.content;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

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

    @Override
    public String getMessage(Player player) {
        return Bundle.format("history.withdraw", findLocale(player.locale), name, amount, Icons.get(content.item(itemID).name), Icons.get(content.block(blockID).name), formatDate(time));
    }
}
