package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import useful.Bundle;

import static darkdustry.features.history.RotateEntry.sides;
import static darkdustry.utils.Utils.formatHistoryDate;
import static mindustry.Vars.content;

public class BlockEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final byte rotation;
    public final boolean breaking;
    public final long time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.name = event.unit.getPlayer().coloredName();
        this.blockID = event.tile.build instanceof ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = (byte) event.tile.build.rotation;
        this.breaking = event.breaking;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        var block = content.block(blockID);
        return Bundle.format(breaking ? "history.deconstruct" : block.rotate ? "history.construct.rotate" : "history.construct", player, name, Icons.get(block), formatHistoryDate(time), sides[rotation]);
    }
}