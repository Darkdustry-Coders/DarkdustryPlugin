package pandorum.entry;

import static pandorum.Misc.findLocale;

import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.type.Item;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.TimeZone;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WithdrawEntry implements HistoryEntry {

    public String name;
    public Block block;
    public Item item;
    public int count;
    public Date time;

    public WithdrawEntry(String name, Block block, Item item, int count) {
        this.name = name;
        this.block = block;
        this.item = item;
        this.count = count;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(this.time);

        return Bundle.format("history.withdraw", findLocale(player.locale), name, count, Icons.get(item.name), block.name, ftime);
    }
}
