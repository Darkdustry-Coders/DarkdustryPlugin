package rewrite.features.history;

import arc.graphics.Color;
import arc.math.geom.Point2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ctype.MappableContent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.units.UnitFactory;

import java.util.Locale;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static rewrite.components.Icons.get;
import static rewrite.components.Bundle.format;
import static rewrite.utils.Find.locale;
import static rewrite.utils.Utils.formatDate;

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

        if (value instanceof Boolean enabled) {
            return enabled ? format("history.config.on", locale, name, get(block.name), date) : format("history.config.off", locale, name, get(block.name), date);
        }

        if (value instanceof String message) {
            return !message.isEmpty() ? format("history.config.message", locale, name, get(block.name), message, date) : format("history.config.default", locale, name, get(block.name), date);
        }

        if (block.configurations.containsKey(Point2.class)) {
            Tile tile = world.tile((int) value);
            return connect ? format("history.config.connect", locale, name, get(block.name), tile.x, tile.y, date) : format("history.config.disconnect", locale, name, get(block.name), date);
        }

        if (block.configurations.containsKey(Point2[].class)) {
            Tile tile = world.tile((int) value);
            return connect ? format("history.config.power-node.connect", locale, name, get(block.name), tile.x, tile.y, date) : format("history.config.power-node.disconnect", locale, name, get(block.name), tile.x, tile.y, date);
        }

        if (block instanceof LightBlock) {
            Color color = Tmp.c1.set((int) value);
            return format("history.config.illuminator", locale, name, get(block.name), color.toString(), date);
        }

        if (block instanceof UnitFactory factory) {
            int buildPlan = (int) value;
            return buildPlan != -1 ? format("history.config", locale, name, get(block.name), get(factory.plans.get(buildPlan).unit.name), date) : format("history.config.default", locale, name, get(block.name), date);
        }

        return format("history.config.default", locale, name, get(block.name), date);
    }
}
