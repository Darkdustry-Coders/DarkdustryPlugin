package pandorum.entry;

import arc.util.*;
import arc.struct.*;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.*;
import mindustry.world.Block;
import pandorum.Misc;

import java.util.concurrent.TimeUnit;
import java.util.Locale;

import pandorum.comp.*;

public class BlockEntry implements HistoryEntry{
    public final long lastAccessTime = Time.millis();
    @Nullable
    public final String name;
    public final Unit unit;
    public final Block block;
    public final boolean breaking;
    public final int rotation;

    public BlockEntry(BlockBuildEndEvent event){
        this.unit = event.unit;
        this.name = unit.isPlayer() ? Misc.colorizedName(unit.getPlayer()) : unit.controller() instanceof Player ? Misc.colorizedName(unit.getPlayer()) : null;
        if (event.tile.build != null) {
            this.block = event.tile.build.block;
            this.rotation = event.tile.build.rotation;
        } else {
            this.block = null;
            this.rotation = -1;
        }
        this.breaking = event.breaking;
        
    }

    @Override
    public String getMessage(Player player){
        if(breaking){
            return name != null ? bundle.format("events.history.block.destroy.player", findLocale(player.locale), name) :
            bundle.format("events.history.block.destroy.unit", findLocale(player.locale), unit.type);
        }

        String base = name != null ? bundle.format("events.history.block.construct.player", findLocale(player.locale), name, block) :
                      bundle.format("events.history.block.construct.unit", findLocale(player.locale), unit.type, block);
        if(block.rotate){
            base += bundle.format("events.history.block.construct.rotate", findLocale(player.locale), RotateEntry.sides[rotation]);
        }
        return base;
    }

    @Override
    public long getLastAccessTime(TimeUnit unit){
        return unit.convert(Time.timeSinceMillis(lastAccessTime), TimeUnit.MILLISECONDS);
    }

    private static Locale findLocale(String code) {
        Locale locale = Structs.find(bundle.supportedLocales, l -> l.toString().equals(code) ||
                code.startsWith(l.toString()));
        return locale != null ? locale : bundle.defaultLocale();
    }
}
