package pandorum.entry;

import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;

import static pandorum.Misc.findLocale;
import static pandorum.Misc.formatTime;

public class DepositEntry implements HistoryEntry {
    public final String name;
    public final Block block;
    public final Item item;
    public final int amount;
    public final Date time;

    public DepositEntry(DepositEvent event) {
        this.name = event.player.coloredName();
        this.block = event.tile.block;
        this.item = event.item;
        this.amount = event.amount;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        return Bundle.format("history.deposit", findLocale(player.locale), name, amount, Icons.get(item.name), block.name, formatTime(time));
    }
}
