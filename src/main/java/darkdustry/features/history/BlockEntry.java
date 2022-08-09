package darkdustry.features.history;

import arc.util.Time;
import mindustry.game.EventType.*;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import darkdustry.components.Icons;
import darkdustry.utils.Find;

import static mindustry.Vars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.features.history.RotateEntry.*;
import static darkdustry.utils.Utils.*;

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
        Block block = content.block(blockID);
        String key = breaking ? "history.block.deconstruct" : block.rotate ? "history.block.construct.rotate" : "history.block.construct";
        return format(key, Find.locale(player.locale), name, Icons.get(block.name), formatDate(time), sides[rotation]);
    }
}
