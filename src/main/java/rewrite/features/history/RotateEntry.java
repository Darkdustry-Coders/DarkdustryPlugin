package rewrite.features.history;

import arc.util.Time;
import mindustry.gen.Iconc;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerAction;
import rewrite.components.Icons;
import rewrite.utils.Find;

import static mindustry.Vars.*;
import static rewrite.components.Bundle.*;
import static rewrite.utils.Utils.*;

public class RotateEntry implements HistoryEntry {

    public static final char[] sides = {Iconc.right, Iconc.up, Iconc.left, Iconc.down};

    public final String name;
    public final short blockID;
    public final int rotation;
    public final long time;

    public RotateEntry(PlayerAction action) {
        this.name = action.player.name;
        this.blockID = action.tile.build.block.id;
        this.rotation = action.rotation;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return format("history.rotate", Find.locale(player.locale), name, Icons.get(content.block(blockID).name), sides[rotation], formatDate(time));
    }
}
