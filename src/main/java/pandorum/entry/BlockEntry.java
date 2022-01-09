package pandorum.entry;

import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;
import java.util.Locale;

import static mindustry.Vars.content;
import static pandorum.Misc.findLocale;
import static pandorum.Misc.formatTime;

public class BlockEntry implements HistoryEntry {

    public final String name;
    public final short unitID;
    public final short blockID;
    public final int rotation;
    public final boolean breaking;
    public final Date date;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getControllerName();
        this.unitID = event.unit.type.id;
        this.blockID = event.breaking ? -1 : event.tile.build.block.id;
        this.rotation = event.breaking ? -1 : event.tile.build.rotation;
        this.breaking = event.breaking;
        this.date = new Date();
    }

    @Override
    public String getMessage(Player player) {
        Block block = content.block(blockID);
        UnitType unit = content.unit(unitID);
        String time = formatTime(date);
        Locale locale = findLocale(player.locale);

        if (breaking) {
            return name != null ? Bundle.format("history.block.destroy.player", locale, name, time) : Bundle.format("history.block.destroy.unit", locale, Icons.get(unit.name), time);
        }

        String base = name != null ? Bundle.format("history.block.construct.player", locale, name, Icons.get(block.name), time) : Bundle.format("history.block.construct.unit", locale, Icons.get(unit.name), Icons.get(block.name), time);
        if (block.rotate) base += Bundle.format("history.block.construct.rotate", locale, RotateEntry.sides[rotation]);

        return base;
    }
}
