package pandorum.entry;

import static pandorum.Misc.findLocale;

import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Block;
import pandorum.comp.Bundle;

import java.util.TimeZone;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RotateEntry implements HistoryEntry {

    public String name;
    public Block block;
    public int rotation;
    public Date time;
    
    protected static final String[] sides;

    static {
        sides = "\uE803, \uE804, \uE802, \uE805".split(", ");
    }

    public RotateEntry(PlayerAction action) {
        this.name = action.player.coloredName();
        this.block = action.tile.build.block;
        this.rotation = action.rotation;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(time);

        return Bundle.format("history.rotate", findLocale(player.locale), name, block.name, sides[rotation], ftime);
    }
}
