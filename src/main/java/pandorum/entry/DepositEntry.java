package pandorum.entry;

import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;

import static mindustry.Vars.content;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

public class DepositEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final Date date;

    public DepositEntry(DepositEvent event) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.date = new Date();
    }

    @Override
    public String getMessage(Player player) {
        return Bundle.format("history.deposit", findLocale(player.locale), name, amount, Icons.get(content.item(itemID).name), Icons.get(content.block(blockID).name), formatDate(date));
    }
}
