package darkdustry.features.history;

import arc.util.Time;
import darkdustry.database.Database;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.world.blocks.ConstructBlock;
import useful.Bundle;

import static darkdustry.utils.Utils.formatRotation;
import static mindustry.Vars.content;
import static mindustry.Vars.netServer;

public class PreBlockEntry implements HistoryEntry {
    public final String uuid;
    public final short blockID;
    public final int rotation;
    public final boolean breaking;
    public final long timestamp;

    public PreBlockEntry(EventType.BlockBuildBeginEvent event) {
        this.uuid = event.unit.getPlayer().uuid();
        this.blockID = event.tile.build instanceof ConstructBlock.ConstructBuild build ? build.current.id : event.tile.blockID();
        this.rotation = event.tile.build.rotation;
        this.breaking = event.breaking;
        this.timestamp = Time.millis();
    }

    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        var data = Database.getPlayerDataOrCreate(uuid);
        var block = content.block(blockID);

        return breaking ?
                Bundle.format("history.breaking", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id) :
                block.rotate ?
                        Bundle.format("history.building.rotate", player, info.lastName, block.emoji(), formatRotation(rotation), Bundle.formatRelative(player, timestamp), data.id) :
                        Bundle.format("history.building", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id);
    }
}
