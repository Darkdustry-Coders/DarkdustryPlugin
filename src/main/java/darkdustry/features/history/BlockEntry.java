package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import useful.Bundle;

import static darkdustry.components.Icons.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class BlockEntry implements HistoryEntry {

    public final String uuid;
    public final short blockID;
    public final int rotation;
    public final boolean breaking;
    public final long timestamp;

    public BlockEntry(BlockBuildEndEvent event) {
        this.uuid = event.unit.getPlayer().uuid();
        this.blockID = event.tile.build instanceof ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = event.tile.build.rotation;
        this.breaking = event.breaking;
        this.timestamp = Time.millis();
    }

    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        var block = content.block(blockID);

        return Bundle.format(breaking ? "history.deconstruct" : block.rotate ? "history.construct.rotate" : "history.construct", player, info.lastName, Icons.icon(block), formatTime(timestamp), sides[rotation]);
    }
}