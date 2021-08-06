package pandorum.entry;

import arc.util.*;
import arc.struct.*;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.gen.*;
import mindustry.world.Block;

import pandorum.comp.*;
import static pandorum.Misc.*;

public class BlockEntry implements HistoryEntry{
    @Nullable
    public final String name;
    public final Unit unit;
    public final Block block;
    public final boolean breaking;
    public final int rotation;

    public BlockEntry(BlockBuildEndEvent event){
        this.unit = event.unit;
        this.name = unit.isPlayer() ? colorizedName(unit.getPlayer()) : unit.controller() instanceof Player ? colorizedName(unit.getPlayer()) : null;
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
            return name != null ? Bundle.format("events.history.block.destroy.player", findLocale(player.locale), name) :
            Bundle.format("events.history.block.destroy.unit", findLocale(player.locale), unit.type);
        }

        String base = name != null ? Bundle.format("events.history.block.construct.player", findLocale(player.locale), name, block) :
                      Bundle.format("events.history.block.construct.unit", findLocale(player.locale), unit.type, block);
        if(block.rotate){
            base += Bundle.format("events.history.block.construct.rotate", findLocale(player.locale), RotateEntry.sides[rotation]);
        }
        return base;
    }
}
