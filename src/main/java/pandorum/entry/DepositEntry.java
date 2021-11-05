package pandorum.entry;

import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;

import static pandorum.Misc.findLocale;

public class DepositEntry implements HistoryEntry {

    public String name;
    public Block block;
    public Item item;
    public int amount;
    public Date time;

    public DepositEntry(DepositEvent event) {
        this.name = event.player.coloredName();
        this.block = event.tile.block;
        this.item = event.item;
        this.amount = event.amount;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        String ftime = Misc.formatTime(time);
        return Bundle.format("history.deposit", findLocale(player.locale), name, amount, Icons.get(item.name), block.name, ftime);
    }
}
