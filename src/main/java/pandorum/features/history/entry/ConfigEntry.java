package pandorum.features.history.entry;

import arc.util.Time;
import arc.util.Tmp;
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
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.storage.Unloader;
import mindustry.world.blocks.units.UnitFactory;
import pandorum.components.Bundle;
import pandorum.components.Icons;

import java.util.Locale;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.formatDate;

public class ConfigEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final Object value;
    public final boolean connect;
    public final long time;

    public ConfigEntry(ConfigEvent event, boolean connect) {
        this.name = event.player.name;
        this.blockID = event.tile.block.id;
        this.value = event.value;
        this.connect = connect;
        this.time = Time.millis();
    }

    // TODO это конечно хорошо, но тут нету блоков из в7. У них свои значения конфигов. Надо их добавить.
    @Override
    public String getMessage(Player player) {
        Block block = content.block(blockID);
        String date = formatDate(time);
        Locale locale = findLocale(player.locale);

        if (block instanceof PowerNode) {
            Tile tile = world.tile((int) value);
            return connect ? Bundle.format("history.config.power-node.connect", locale, name, Icons.get(block.name), tile.x, tile.y, date) : Bundle.format("history.config.power-node.disconnect", locale, name, Icons.get(block.name), tile.x, tile.y, date);
        }

        if (block instanceof ItemBridge || block instanceof MassDriver || block instanceof PayloadMassDriver) {
            int data = (int) value;
            if (data < 0) {
                return Bundle.format("history.config.disconnect", locale, name, Icons.get(block.name), date);
            }

            Tile tile = world.tile(data);
            return Bundle.format("history.config.connect", locale, name, Icons.get(block.name), tile.x, tile.y, date);
        }

        if (block instanceof Door) {
            boolean opened = (boolean) value;
            return opened ? Bundle.format("history.config.door.on", locale, name, Icons.get(block.name), date) : Bundle.format("history.config.door.off", locale, name, Icons.get(block.name), date);
        }

        if (block instanceof SwitchBlock) {
            boolean enabled = (boolean) value;
            return enabled ? Bundle.format("history.config.switch.on", locale, name, Icons.get(block.name), date) : Bundle.format("history.config.switch.off", locale, name, Icons.get(block.name), date);
        }

        if (block instanceof LightBlock) {
            Tmp.c1.set((int) value);
            return Bundle.format("history.config.illuminator", locale, name, Icons.get(block.name), Tmp.c1.toString(), Tmp.c1.toString(), date);
        }

        if (block instanceof MessageBlock) {
            String message = (String) value;
            if (message.isBlank()) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
            }

            return Bundle.format("history.config.message", locale, name, Icons.get(block.name), message, date);
        }

        if (block instanceof LiquidSource) {
            Liquid liquid = (Liquid) value;
            if (liquid == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(liquid.name), date);
        }

        if (block instanceof ItemSource || block instanceof Sorter || block instanceof Unloader) {
            Item item = (Item) value;
            if (item == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(item.name), date);
        }

        if (block instanceof Constructor) {
            Block buildPlan = (Block) value;
            if (buildPlan == null) {
                return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
            }

            return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), date);
        }

        if (block instanceof UnitFactory factory) {
            if (value instanceof UnitType buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), date);
            } else if (value instanceof Integer buildPlan) {
                return buildPlan >= 0 && buildPlan < factory.plans.size ? Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(factory.plans.get(buildPlan).unit.name), date) : Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
            }

            return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
        }

        if (block instanceof PayloadSource) {
            if (value instanceof Block buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), date);
            } else if (value instanceof UnitType buildPlan) {
                return Bundle.format("history.config", locale, name, Icons.get(block.name), Icons.get(buildPlan.name), date);
            }

            return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
        }

        return Bundle.format("history.config.default", locale, name, Icons.get(block.name), date);
    }
}
