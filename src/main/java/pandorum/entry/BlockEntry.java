package pandorum.entry;

import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Block;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;
import java.util.Locale;

import static pandorum.Misc.findLocale;
import static pandorum.Misc.formatTime;

public class BlockEntry implements HistoryEntry {

    public final Unit unit;
    public final boolean isPlayer;
    public final String name;
    public final boolean breaking;
    public final Block block;
    public final int rotation;
    public final Date time;

    public BlockEntry(BlockBuildEndEvent event) {
        this.unit = event.unit;
        this.isPlayer = unit.isPlayer();
        this.name = isPlayer ? unit.getPlayer().coloredName() : null;
        this.breaking = event.breaking;
        this.block = breaking ? null : event.tile.build.block;
        this.rotation = breaking ? 0 : event.tile.build.rotation;
        this.time = new Date();
    }

    @Override
    public String getMessage(Player player) {
        String ftime = formatTime(time);
        Locale locale = findLocale(player.locale);

        if (breaking) {
            return isPlayer ? Bundle.format("history.block.destroy.player", locale, name, ftime) : Bundle.format("history.block.destroy.unit", locale, Icons.get(unit.type.name), unit.type.name, ftime);
        }

        StringBuilder base = new StringBuilder(isPlayer ? Bundle.format("history.block.construct.player", locale, name, block, ftime) : Bundle.format("history.block.construct.unit", locale, Icons.get(unit.type.name), unit.type.name, block, ftime));
        if (block.rotate) {
            base.append(Bundle.format("history.block.construct.rotate", locale, RotateEntry.sides[rotation]));
        }
        return base.toString();
    }
}
