package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import mindustry.gen.*;
import mindustry.net.Administration.PlayerAction;
import useful.Bundle;

import static darkdustry.utils.Utils.formatShortDate;
import static mindustry.Vars.content;

public class RotateEntry implements HistoryEntry {

    public static final char[] sides = {Iconc.right, Iconc.up, Iconc.left, Iconc.down};

    public final String name;
    public final short blockID;
    public final byte rotation;
    public final long time;

    public RotateEntry(PlayerAction action) {
        this.name = action.player.coloredName();
        this.blockID = action.tile.blockID();
        this.rotation = (byte) action.rotation;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return Bundle.format("history.rotate", player, name, Icons.icon(content.block(blockID)), sides[rotation], formatShortDate(time));
    }
}