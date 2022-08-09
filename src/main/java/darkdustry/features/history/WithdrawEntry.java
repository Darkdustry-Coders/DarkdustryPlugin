package darkdustry.features.history;

import arc.util.Time;
import mindustry.game.EventType.*;
import mindustry.gen.Player;
import darkdustry.components.Icons;
import darkdustry.utils.Find;

import static mindustry.Vars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.utils.Utils.*;

public class WithdrawEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final short itemID;
    public final int amount;
    public final long time;

    public WithdrawEntry(WithdrawEvent event) {
        this.name = event.player.name;
        this.blockID = event.tile.block.id;
        this.itemID = event.item.id;
        this.amount = event.amount;
        this.time = Time.millis();
    }

    public String getMessage(Player player) {
        return format("history.withdraw", Find.locale(player.locale), name, amount, Icons.get(content.item(itemID).name), Icons.get(content.block(blockID).name), formatDate(time));
    }
}
