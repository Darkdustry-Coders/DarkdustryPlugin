package darkdustry.features.history;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.*;
import darkdustry.database.Database;
import mindustry.ai.UnitCommand;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.logic.CanvasBlock.CanvasBuild;
import mindustry.world.blocks.logic.LogicBlock.LogicBuild;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryBuild;
import useful.Bundle;

import static mindustry.Vars.*;

public class ConfigEntry implements HistoryEntry {
    public final String uuid;
    public final short blockID;
    public final Object config;
    public final long timestamp;

    public ConfigEntry(ConfigEvent event) {
        this.uuid = event.player.uuid();
        this.blockID = event.tile.block.id;
        this.config = getConfig(event);
        this.timestamp = Time.millis();
    }

    // Ифы сила, Дарк могила
    // (C) Овлер, 2021 год до н.э.
    @Override
    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        var data = Database.getPlayerDataOrCreate(uuid);
        var block = content.block(blockID);

        return switch (config) {
            case UnlockableContent content -> Bundle.format("history.config", player, info.lastName, block.emoji(), content.emoji(), Bundle.formatRelative(player, timestamp), data.id);

            case Boolean enabled -> enabled ?
                    Bundle.format("history.config.on", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id) :
                    Bundle.format("history.config.off", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id);

            case String text -> text.isBlank() ?
                    Bundle.format("history.config.default", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id) :
                    Bundle.format("history.config.text", player, info.lastName, block.emoji(), text.replaceAll("\n", " "), Bundle.formatRelative(player, timestamp), data.id);

            case UnitCommand command -> Bundle.format("history.config.command", player, info.lastName, block.emoji(), command.getEmoji(), Bundle.formatRelative(player, timestamp), data.id);

            case Point2 point -> point.pack() == -1 ?
                    Bundle.format("history.config.disconnect", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id) :
                    Bundle.format("history.config.connect", player, info.lastName, block.emoji(), point, Bundle.formatRelative(player, timestamp), data.id);

            case Point2[] points -> points.length == 0 ?
                    Bundle.format("history.config.disconnect", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id) :
                    Bundle.format("history.config.connect", player, info.lastName, block.emoji(), Seq.with(points).toString(", "), Bundle.formatRelative(player, timestamp), data.id);

            case null, default -> switch (block) {
                case LightBlock ignored -> Bundle.format("history.config.color", player, info.lastName, block.emoji(), Tmp.c1.set((int) config), Bundle.formatRelative(player, timestamp), data.id);
                case LogicBlock ignored -> Bundle.format("history.config.code", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id);
                case CanvasBlock ignored -> Bundle.format("history.config.image", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id);

                default -> Bundle.format("history.config.default", player, info.lastName, block.emoji(), Bundle.formatRelative(player, timestamp), data.id);
            };
        };
    }

    public Object getConfig(ConfigEvent event) {
        if (event.tile instanceof LogicBuild || event.tile instanceof CanvasBuild)
            return null;

        if (event.tile instanceof UnitFactoryBuild factory)
            return factory.unit();

        if (event.tile.config() instanceof Point2 point)
            return point.add(event.tile.tileX(), event.tile.tileY());

        if (event.tile.config() instanceof Point2[] points) {
            Structs.each(point -> point.add(event.tile.tileX(), event.tile.tileY()), points);
            return points;
        }

        return event.tile.config();
    }
}