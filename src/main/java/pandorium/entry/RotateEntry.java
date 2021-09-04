package pandorium.entry;

import static pandorium.Misc.findLocale;

import mindustry.gen.Player;
import mindustry.world.Block;
import pandorium.comp.Bundle;

import java.util.TimeZone;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RotateEntry implements HistoryEntry{

    public String name;
    public Block block;
    public int rotation;
    public Date time;
    
    protected static final String[] sides;

    static{
        sides = Bundle.get("events.history.rotate.all", Bundle.defaultLocale()).split(", ");
    }

    public RotateEntry(String name, Block block, int rotation){
        this.name = name;
        this.block = block;
        this.rotation = rotation;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player){
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(this.time);

        return Bundle.format("events.history.rotate", findLocale(player.locale), name, block.name, sides[rotation], ftime);
    }
}
