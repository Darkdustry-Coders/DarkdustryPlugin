package pandorum.entry;

import arc.util.*;
import arc.struct.*;
import mindustry.world.Block;
import mindustry.gen.*;
import pandorum.struct.Tuple2;

import pandorum.comp.*;
import static pandorum.Misc.*;

public class RotateEntry implements HistoryEntry{
    protected static final String[] sides;

    static{
        sides = bundle.get("events.history.rotate.all").split(", ");
    }

    public final String name;
    public final Block block;
    public final int rotation;

    public RotateEntry(String name, Block block, int rotation){
        this.name = name;
        this.block = block;
        this.rotation = rotation;
    }

    @Override
    public String getMessage(Player player){
        return bundle.format("events.history.rotate", findLocale(player.locale), name, block.name, sides[rotation]);
    }
}
