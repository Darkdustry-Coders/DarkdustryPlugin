package pandorum.entry;

import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Block;
import pandorum.Misc;
import pandorum.comp.Bundle;

import java.util.Date;

import static pandorum.Misc.findLocale;

public class RotateEntry implements HistoryEntry {

    public final String name;
    public final Block block;
    public final int rotation;
    public final Date time;
    
    public static final String[] sides = {"\uE803", "\uE804", "\uE802", "\uE805"};

    public RotateEntry(PlayerAction action) {
        this.name = action.player.coloredName();
        this.block = action.tile.build.block;
        this.rotation = action.rotation;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        String ftime = Misc.formatTime(time);
        return Bundle.format("history.rotate", findLocale(player.locale), name, block.name, sides[rotation], ftime);
    }
}
