package pandorum.entry;

import arc.util.Pack;
import mindustry.entities.units.UnitCommand;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.distribution.Sorter;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.blocks.logic.SwitchBlock;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.payloads.PayloadSource;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.storage.Unloader;
import mindustry.world.blocks.units.CommandCenter;
import mindustry.world.blocks.units.UnitFactory;
import pandorum.comp.Bundle;
import pandorum.comp.Icons;

import java.util.Date;
import java.util.Locale;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.Misc.findLocale;
import static pandorum.Misc.formatTime;

public class ConfigEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final Object value;
    public final boolean connect;
    public final Date time;

    public ConfigEntry(ConfigEvent event, boolean connect) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block().id;
        this.value = getConfig(event);
        this.connect = connect;
        this.time = new Date();
    }

    private Object getConfig(ConfigEvent event) {
        if (event.tile.block() instanceof PowerNode) {
            return Pack.longInt(event.tile.power.links.size, (int) event.value);
        }
        return event.value;
    }

    @Override
    public String getMessage(Player player) {
        Block block = content.block(blockID);
        String ftime = formatTime(time);
        Locale locale = findLocale(player.locale);

        if (block instanceof PowerNode) {
            Tile tile = world.tile(Pack.rightInt((long) value));
            return connect ? Bundle.format("history.config.power-node.connect", locale, name, Icons.get(block.name), tile.x, tile.y, ftime) : Bundle.format("history.config.power-node.disconnect", locale, name, Icons.get(block.name), tile.x, tile.y, ftime);
        }

        if (block instanceof ItemBridge || block instanceof MassDriver || block instanceof PayloadMassDriver) {
            int data = (int) value;
            if (data < 0) {
                return Bundle.format("history.config.disconnect", locale, name, Icons.get(block.name), ftime);
            }

            Tile tile = world.tile(data);
            return Bundle.format("history.config.connect", locale, name, Icons.get(block.name), tile.x, tile.y, ftime);
        }

        if (block instanceof Door) {
            boolean data = (boolean) value;
            return data ? Bundle.format("history.config.door.on", locale, name, Icons.get(block.name), ftime) : Bundle.format("history.config.door.off", locale, name, Icons.get(block.name), ftime);
        }

        if (block instanceof SwitchBlock) {
            boolean data = (boolean) value;
            return data ? Bundle.format("history.config.switch.on", locale, name, Icons.get(block.name), ftime) : Bundle.format("history.config.switch.off", locale, name, Icons.get(block.name), ftime);
        }

        if (block instanceof CommandCenter) {
            UnitCommand command = (UnitCommand) value;
            return Bundle.format("history.config.command-center", locale, name, Icons.get(command.name()), ftime);
        }

        if (block instanceof MessageBlock) {
            String message = (String) value;
            if (message.isBlank()) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
            }

            return Bundle.format("history.config.message", locale, name, Icons.get(block.name), message, ftime);
        }

        if (block instanceof LiquidSource) {
            Liquid liquid = (Liquid) value;
            if (liquid == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(liquid.name), ftime);
        }

        if (block instanceof Unloader || block instanceof Sorter || block instanceof ItemSource) {
            Item item = (Item) value;
            if (item == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(item.name), ftime);
        }

        if (block instanceof Constructor) {
            Block buildPlan = (Block) value;
            if (buildPlan == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), ftime);
        }

        if (block instanceof UnitFactory factory) {
            if (value instanceof UnitType buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), ftime);
            } else if (value instanceof Integer buildPlan) {
                return buildPlan < 0 || buildPlan > factory.plans.size ? Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime) : Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(factory.plans.get(buildPlan).unit.name), ftime);
            }

            return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
        }

        if (block instanceof PayloadSource) {
            if (value instanceof Block buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), ftime);
            } else if (value instanceof UnitType buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), ftime);
            }

            return Bundle.format("history.config.default", locale, name, Icons.get(block.name), ftime);
        }

        return Bundle.format("history.config.changed", locale, name, Icons.get(block.name), ftime);
    }
}
