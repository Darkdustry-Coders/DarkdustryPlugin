package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;
import useful.Bundle;

import static darkdustry.components.Icons.sides;
import static darkdustry.utils.Utils.formatShortDate;
import static mindustry.Vars.*;

public class RotateEntry implements HistoryEntry {

    public final String uuid;
    public final short blockID;
    public final byte rotation;
    public final long time;

    public RotateEntry(PlayerAction action) {
        this.uuid = action.player.uuid();
        this.blockID = action.tile.blockID();
        this.rotation = (byte) action.rotation;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        return Bundle.format("history.rotate", player, info.lastName, Icons.icon(content.block(blockID)), sides.get(rotation), formatShortDate(time));
    }
}