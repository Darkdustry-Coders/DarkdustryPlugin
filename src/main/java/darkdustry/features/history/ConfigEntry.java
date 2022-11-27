package darkdustry.features.history;

import arc.math.geom.Point2;
import arc.util.*;
import darkdustry.components.Icons;
import mindustry.ctype.MappableContent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.units.UnitFactory;
import useful.Bundle;

import java.util.Arrays;

import static darkdustry.utils.Utils.formatHistoryDate;
import static mindustry.Vars.content;

public class ConfigEntry implements HistoryEntry {

    public final String name;
    public final short blockID;
    public final Object value;
    public final boolean connect;
    public final long time;

    public ConfigEntry(ConfigEvent event) {
        this.name = event.player.coloredName();
        this.blockID = event.tile.block.id;
        this.value = getValue(event);
        this.connect = value instanceof Point2 point && getConnect(event, point);
        this.time = Time.millis();
    }

    // Ифы сила, Дарк могила
    // (C) Овлер, 2021 год до н.э.
    @Override
    public String getMessage(Player player) {
        var block = content.block(blockID);
        var date = formatHistoryDate(time);

        if (value instanceof MappableContent content) {
            return Bundle.format("history.config", player, name, Icons.get(block), Icons.get(content), date);
        }

        if (value instanceof Boolean on) {
            return on ? Bundle.format("history.config.on", player, name, Icons.get(block), date) : Bundle.format("history.config.off", player, name, Icons.get(block), date);
        }

        if (value instanceof String text) {
            return !text.isEmpty() ? Bundle.format("history.config.text", player, name, Icons.get(block), text, date) : Bundle.format("history.config.default", player, name, Icons.get(block), date);
        }

        if (value instanceof Point2 point) {
            return connect ? Bundle.format("history.config.connect", player, name, Icons.get(block), point.x, point.y, date) : Bundle.format("history.config.disconnect", player, name, Icons.get(block), date);
        }

        if (value instanceof Point2[] points) {
            return points.length > 0 ? Bundle.format("history.config.connects", player, name, Icons.get(block), Arrays.toString(points), date) : Bundle.format("history.config.disconnect", player, name, Icons.get(block), date);
        }

        if (block instanceof LightBlock) {
            return Bundle.format("history.config.color", player, name, Icons.get(block), Tmp.c1.set((int) value).toString(), date);
        }

        if (block instanceof LogicBlock) {
            return Bundle.format("history.config.code", player, name, Icons.get(block), date);
        }

        if (block instanceof CanvasBlock) {
            return Bundle.format("history.config.image", player, name, Icons.get(block), date);
        }

        return Bundle.format("history.config.default", player, name, Icons.get(block), date);
    }

    public Object getValue(ConfigEvent event) {
        if (event.tile.block instanceof LogicBlock || event.tile.block instanceof CanvasBlock)
            return null;

        if (event.value instanceof Integer number) {
            if (event.tile.block instanceof UnitFactory factory)
                return number < 0 || number >= factory.plans.size ? null : factory.plans.get(number).unit;
            if (event.tile.block.configurations.containsKey(Point2.class) || event.tile.block.configurations.containsKey(Point2[].class))
                return Point2.unpack(number);
        }

        if (event.value instanceof Point2 point)
            return point.add(event.tile.tileX(), event.tile.tileY());

        if (event.value instanceof Point2[] points) {
            Structs.each(point -> point.add(event.tile.tileX(), event.tile.tileY()), points);
            return points;
        }

        return event.value;
    }

    public boolean getConnect(ConfigEvent event, Point2 point) {
        if (event.tile.block.configurations.containsKey(Point2.class))
            return point.pack() != -1 && point.pack() != event.tile.pos();

        if (event.tile.block.configurations.containsKey(Point2[].class))
            return Structs.contains((Point2[]) event.tile.config(), point.cpy().sub(event.tile.tileX(), event.tile.tileY())::equals);

        return false;
    }
}