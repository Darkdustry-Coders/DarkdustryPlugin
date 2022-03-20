package pandorum.antigrief.history.entry;

import arc.util.Time;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.type.UnitType;
import mindustry.world.Block;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.util.Locale;

import static mindustry.Vars.content;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

public class BlockEntry implements HistoryEntry {
    public final String name;
    public final short unitID;
    public final short blockID;
    public final byte rotation;
    public final boolean breaking;
    public final long time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getControllerName();
        this.unitID = event.unit.type.id;
        this.blockID = event.breaking ? -1 : event.tile.blockID();
        this.rotation = (byte) (event.breaking ? -1 : event.tile.build.rotation);
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        Block block = content.block(blockID);
        UnitType unit = content.unit(unitID);
        String date = formatDate(time);
        Locale locale = findLocale(player.locale);

        if (breaking) {
            return name != null ? Bundle.format("history.block.deconstruct.player", locale, name, date) : Bundle.format("history.block.deconstruct.unit", locale, Icons.get(unit.name), date);
        }

        String base = name != null ? Bundle.format("history.block.construct.player", locale, name, Icons.get(block.name), date) : Bundle.format("history.block.construct.unit", locale, Icons.get(unit.name), Icons.get(block.name), date);
        if (block.rotate) base += Bundle.format("history.block.construct.rotate", locale, RotateEntry.sides[rotation]);
        return base;
    }
}
