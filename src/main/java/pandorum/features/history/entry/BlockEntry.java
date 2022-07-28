package pandorum.features.history.entry;

import arc.util.Time;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.util.Locale;

import static mindustry.Vars.content;
import static pandorum.features.history.entry.RotateEntry.sides;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

public class BlockEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final int rotation;
    public final boolean breaking;
    public final long time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getPlayer().name;
        this.blockID = event.tile.build instanceof ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = event.tile.build.rotation;
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        Block block = content.block(blockID);
        String date = formatDate(time);
        Locale locale = findLocale(player.locale);

        return breaking ? Bundle.format("history.block.deconstruct", locale, name, Icons.get(block.name), date) : block.rotate ? Bundle.format("history.block.construct.rotate", locale, name, Icons.get(block.name), sides[rotation], date) : (Bundle.format("history.block.construct", locale, name, Icons.get(block.name), date));
    }
}
