package darkdustry.features.history;

import arc.util.Time;
import darkdustry.database.Database;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import useful.Bundle;

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
        var data = Database.getPlayerDataOrCreate(uuid);
        var block = content.block(blockID);

        return breaking ?
                Bundle.format("history.broke", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), "" + data.id) :
                block.rotate ?
                        Bundle.format("history.built.rotate", player, info.lastName, block.emoji(), formatRotation(rotation), Bundle.formatRelative(player, timestamp), "" + data.id) :
                        Bundle.format("history.built", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), "" + data.id);
    }
}