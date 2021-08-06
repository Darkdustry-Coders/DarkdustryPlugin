package pandorum.entry;

import static pandorum.Misc.findLocale;

import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.comp.Bundle;

public class RotateEntry implements HistoryEntry{
    protected static final String[] sides;

    static{
        sides = Bundle.get("events.history.rotate.all").split(", ");
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
        return Bundle.format("events.history.rotate", findLocale(player.locale), name, block.name, sides[rotation]);
    }
}
