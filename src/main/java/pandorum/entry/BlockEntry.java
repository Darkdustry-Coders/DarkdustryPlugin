package pandorum.entry;

import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static pandorum.Misc.findLocale;

public class BlockEntry implements HistoryEntry {
    public final boolean isPlayer;
    public final String name;
    public final Unit unit;
    public final Block block;
    public final boolean breaking;
    public final int rotation;
    public Date time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.breaking = event.breaking;
        this.unit = event.unit;
        this.isPlayer = unit.isPlayer();
        this.name = isPlayer ? unit.getPlayer().coloredName() : null;
        this.block = this.breaking ? null : event.tile.build.block;
        this.rotation = this.breaking ? -1 : event.tile.build.rotation;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        final String ftime = df.format(time);

        if (breaking) {
            return isPlayer ? Bundle.format("history.block.destroy.player", findLocale(player.locale), name, ftime) :
            Bundle.format("history.block.destroy.unit", findLocale(player.locale), Icons.get(unit.type.name), unit.type.name, ftime);
        }

        StringBuilder base = new StringBuilder(isPlayer ? Bundle.format("history.block.construct.player", findLocale(player.locale), name, block, ftime) : Bundle.format("history.block.construct.unit", findLocale(player.locale), Icons.get(unit.type.name), unit.type.name, block, ftime));
        if (block.rotate) {
            base.append(Bundle.format("history.block.construct.rotate", findLocale(player.locale), RotateEntry.sides[rotation]));
        }
        return base.toString();
    }
}
