package pandorum.features.antigrief.history.entry;

import arc.util.Time;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.util.Locale;

import static mindustry.Vars.content;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

public class BlockEntry implements HistoryEntry {
    public final String name;
    public final short blockID;
    public final byte rotation;
    public final boolean breaking;
    public final long time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getPlayer().name;
        this.blockID = event.breaking ? -1 : event.tile.blockID();
        this.rotation = (byte) (event.breaking ? -1 : event.tile.build.rotation);
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        Block block = content.block(blockID);
        String date = formatDate(time);
        Locale locale = findLocale(player.locale);

        if (breaking) {
            return Bundle.format("history.block.deconstruct", locale, name, date);
        }

        String base = Bundle.format("history.block.construct", locale, name, Icons.get(block.name), date);
        if (block.rotate) base += Bundle.format("history.block.construct.rotate", locale, RotateEntry.sides[rotation]);
        return base;
    }
}
