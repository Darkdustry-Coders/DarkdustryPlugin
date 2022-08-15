package darkdustry.features.history;

import arc.math.geom.Point2;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ctype.MappableContent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.units.UnitFactory;

import java.util.Arrays;
import java.util.Locale;

import static darkdustry.components.Bundle.format;
import static darkdustry.components.Icons.get;
import static darkdustry.utils.Find.locale;
import static darkdustry.utils.Utils.formatDate;
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
        Block block = content.block(blockID);
        String date = formatDate(time);
        Locale locale = locale(player.locale);

        if (value == null) {
            return format("history.config.default", locale, name, get(block.name), date);
        }

        if (value instanceof MappableContent content) {
            return format("history.config", locale, name, get(block.name), get(content.name), date);
        }

        if (value instanceof Boolean on) {
            return on ? format("history.config.on", locale, name, get(block.name), date) : format("history.config.off", locale, name, get(block.name), date);
        }

        if (value instanceof String text) {
            return !text.isEmpty() ? format("history.config.text", locale, name, get(block.name), text, date) : format("history.config.default", locale, name, get(block.name), date);
        }

        if (value instanceof Point2 point) {
            return connect ? format("history.config.connect", locale, name, get(block.name), point.x, point.y, date) : format("history.config.disconnect", locale, name, get(block.name), date);
        }

        if (value instanceof Point2[] points) {
            if (points.length == 0) {
                return format("history.config.disconnect", locale, name, get(block.name), date);
            }

            return format("history.config.connects", locale, name, get(block.name), Arrays.toString(points), date);
        }

        if (block instanceof LightBlock) {
            return format("history.config.color", locale, name, get(block.name), Tmp.c1.set((int) value).toString(), date);
        }

        if (block instanceof LogicBlock) {
            return format("history.config.code", locale, name, get(block.name), date);
        }

        return format("history.config.default", locale, name, get(block.name), date);
    }

    public static Object getValue(ConfigEvent event) {
        if (event.value instanceof Integer number) {
            if (event.tile.block instanceof UnitFactory factory)
                return number == -1 ? null : factory.plans.get(number).unit;
            if (event.tile.block.configurations.containsKey(Point2.class) || event.tile.block.configurations.containsKey(Point2[].class)) {
                return Point2.unpack(number);
            }
        }

        if (event.value instanceof Point2 point) {
            return point.add(event.tile.tileX(), event.tile.tileY());
        }

        if (event.value instanceof Point2[] points) {
            Structs.each(point -> point.add(event.tile.tileX(), event.tile.tileY()), points);
            return points;
        }

        return event.value;
    }

    public static boolean getConnect(ConfigEvent event, Point2 point) {
        if (event.tile.block.configurations.containsKey(Point2.class)) {
            return point.pack() != -1 && point.pack() != event.tile.pos();
        }

        if (event.tile.block.configurations.containsKey(Point2[].class)) {
            return Structs.contains((Point2[]) event.tile.config(), point.cpy().sub(event.tile.tileX(), event.tile.tileY())::equals);
        }

        return false;
    }
}
