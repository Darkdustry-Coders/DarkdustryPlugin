package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import darkdustry.utils.Find;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;

import static darkdustry.components.Bundle.format;
import static darkdustry.features.history.RotateEntry.sides;
import static darkdustry.utils.Utils.formatDate;
import static mindustry.Vars.content;

public class BlockEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final int rotation;
    public final boolean breaking;
    public final long time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getPlayer().coloredName();
        this.blockID = event.tile.build instanceof ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = event.tile.build.rotation;
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        var block = content.block(blockID);
        String key = breaking ? "history.deconstruct" : block.rotate ? "history.construct.rotate" : "history.construct";
        return format(key, Find.locale(player.locale), name, Icons.get(block.name), formatDate(time), sides[rotation]);
    }
}
