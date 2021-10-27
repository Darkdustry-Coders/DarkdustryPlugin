package pandorum.entry;

import static pandorum.Misc.findLocale;

import mindustry.game.EventType.DepositEvent;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.type.Item;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.TimeZone;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public String getMessage(Player player){
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(time);

        return Bundle.format("history.deposit", findLocale(player.locale), name, amount, Icons.get(item.name), block.name, ftime);
    }
}
