package pandorum.entry;

import arc.math.geom.Point2;
import arc.util.Pack;
import mindustry.content.Blocks;
import mindustry.entities.units.UnitCommand;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.units.UnitFactory;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;
import java.util.Locale;

import static mindustry.Vars.world;
import static pandorum.Misc.findLocale;

public class ConfigEntry implements HistoryEntry {
    public final String name;
    public final Block block;
    public final Object value;
    public final boolean connect;
    public final Date time;

    public ConfigEntry(ConfigEvent event, boolean connect) {
        this.name = event.player.coloredName();
        this.block = event.tile.block();
        this.value = event.tile instanceof UnitFactory.UnitFactoryBuild factory ? factory.unit() : getConfig(event);
        this.connect = connect;
        this.time = new Date();
    }

    private Object getConfig(ConfigEvent event) {
        if (event.tile.block().configurations.containsKey(Integer.class) && (event.tile.block().configurations.containsKey(Point2[].class) || event.tile.block().configurations.containsKey(Point2.class))) {
            int count;
            if (event.tile.block() instanceof PowerNode) {
                count = event.tile.power.links.size;
            } else {
                count = (int) event.value;
            }
            return Pack.longInt(count, (int) event.value);
        }
        return event.value;
    }

    @Override
    public String getMessage(Player player){
        String ftime = Misc.formatTime(time);
        Locale locale = findLocale(player.locale);

        if (block.configurations.containsKey(Integer.class) && (block.configurations.containsKey(Point2[].class) || block.configurations.containsKey(Point2.class))) {
            int data = Pack.rightInt((long) value);
            if (data < 0) {
                return Bundle.format("history.config.disconnect", locale, name, block, ftime);
            }

            Tile tile = world.tile(data);
            if (tile == null) {
                return Bundle.format("history.config.changed", locale, name, ftime);
            }

            if (connect) {
                return Bundle.format("history.config.connect", locale, name, block, tile.x, tile.y, ftime);
            }

            return Bundle.format("history.config.power-node.disconnect", locale, name, block, tile.x, tile.y, ftime);
        }

        if (block instanceof Door) {
            boolean data = (boolean) value;
            return data ? Bundle.format("history.config.door.on", locale, name, block, ftime) : Bundle.format("history.config.door.off", locale, name, block, ftime);
        }

        if (block == Blocks.switchBlock) {
            boolean data = (boolean) value;
            return data ? Bundle.format("history.config.switch.on", locale, name, ftime) : Bundle.format("history.config.switch.off", locale, name, ftime);
        }

        if (block == Blocks.commandCenter) {
            final String[] commands = Bundle.format("history.config.command-center.all", locale).split(", ");
            return Bundle.format("history.config.command-center", locale, name, commands[((UnitCommand)value).ordinal()], ftime);
        }

        if (block == Blocks.message) {
            String message = (String) value;
            if (message == null || message.isBlank()) {
                return Bundle.format("history.config.default", locale, name, ftime);
            }

            return Bundle.format("history.config.message", locale, name, message.length() > 15 ? message.substring(0, 16) + "..." : message, ftime);
        }

        if (block == Blocks.liquidSource) {
            Liquid liquid = (Liquid) value;
            if (liquid == null) {
                return Bundle.format("history.config.default", locale, name, ftime);
            }

            return Bundle.format("history.config", locale, name, Icons.get(liquid.name), ftime);
        }

        if (block == Blocks.unloader || block == Blocks.sorter || block == Blocks.invertedSorter || block == Blocks.itemSource) {
            Item item = (Item) value;
            if (item == null) {
                return Bundle.format("history.config.default", locale, name, ftime);
            }

            return Bundle.format("history.config", locale, name, Icons.get(item.name), ftime);
        }

        if (block instanceof UnitFactory) {
            UnitType unit = (UnitType) value;
            if (unit == null) {
                return Bundle.format("history.config.default", locale, name, ftime);
            }
            return Bundle.format("history.config.unit", locale, name, Icons.get(unit.name), ftime);
        }
        return Bundle.format("history.config.changed", locale, name, ftime);
    }
}
