package pandorum.entry;

import mindustry.game.EventType.WithdrawEvent;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;

import static mindustry.Vars.content;
import static pandorum.Misc.findLocale;
import static pandorum.Misc.formatTime;

public class WithdrawEntry implements CacheEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long date;

    public WithdrawEntry(WithdrawEvent event) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.date = new Date().getTime();
    }

    @Override
    public String getMessage(Player player) {
        return Bundle.format("history.withdraw", findLocale(player.locale), name, amount, Icons.get(content.item(itemID).name), Icons.get(content.block(blockID).name), formatTime(new Date(date)));
    }
}
