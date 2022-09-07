package darkdustry.features.history;

import arc.util.Time;
import darkdustry.components.Icons;
import darkdustry.utils.Find;
import mindustry.gen.*;
import mindustry.net.Administration.PlayerAction;

import static darkdustry.components.Bundle.format;
import static darkdustry.utils.Utils.formatDate;
import static mindustry.Vars.content;

public class RotateEntry implements HistoryEntry {

    public static final char[] sides = {Iconc.right, Iconc.up, Iconc.left, Iconc.down};

    public final String name;
    public final short blockID;
    public final int rotation;
    public final long time;

    public RotateEntry(PlayerAction action) {
        this.name = action.player.coloredName();
        this.blockID = action.tile.build.block.id;
        this.rotation = action.rotation;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return format("history.rotate", Find.locale(player.locale), name, Icons.get(content.block(blockID)), sides[rotation], formatDate(time));
    }
}
