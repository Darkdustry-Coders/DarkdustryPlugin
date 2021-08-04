package pandorum.entry;

import arc.util.*;
import arc.struct.*;
import mindustry.world.Block;
import mindustry.gen.*;
import pandorum.struct.Tuple2;

import java.util.concurrent.TimeUnit;
import java.util.Locale;

import pandorum.comp.*;;

public class RotateEntry implements HistoryEntry{
    protected static final String[] sides;

    static{
        sides = bundle.get("events.history.rotate.all").split(", ");
    }

    public final String name;
    public final Block block;
    public final int rotation;
    public long lastAccessTime = Time.millis();

    public RotateEntry(String name, Block block, int rotation){
        this.name = name;
        this.block = block;
        this.rotation = rotation;
    }

    @Override
    public String getMessage(Player player){
        return bundle.format("events.history.rotate", findLocale(player.locale), name, block.name, sides[rotation]);
    }

    @Override
    public long getLastAccessTime(TimeUnit unit){
        return unit.convert(Time.timeSinceMillis(lastAccessTime), TimeUnit.MILLISECONDS);
    }

    private static Locale findLocale(String code) {
        Locale locale = Structs.find(bundle.supportedLocales, l -> l.toString().equals(code) ||
                code.startsWith(l.toString()));
        return locale != null ? locale : bundle.defaultLocale();
    }
}
