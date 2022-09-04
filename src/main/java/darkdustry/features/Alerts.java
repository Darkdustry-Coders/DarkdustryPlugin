package darkdustry.features;

import arc.func.Boolp;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.util.Interval;
import darkdustry.components.Icons;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.stream.StreamSupport;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.components.MongoDB.*;
import static mindustry.Vars.state;

public class Alerts {

    public static final Interval alertsInterval = new Interval();

    /**
     * Блоки, которые опасно строить рядом с ядром.
     */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();
    /**
     * Блоки, в которые опасно переносить конкретные ресурсы.
     */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static boolean enabled() {
        return config.mode.isDefault();
    }

    public static void load() {
        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (!enabled() || !isDangerous(event.builder.buildPlan().block, event.team, event.tile) || !alertsInterval.get(60f * alertsTimer))
            return;

        var ids = StreamSupport.stream(event.team.data().players.spliterator(), false)
                .map(Player::uuid)
                .toList();

        getPlayersData(ids).doOnNext(data -> {
            if (data.alertsEnabled) {
                Player player = Groups.player.find(pl-> pl.uuid().equals(data.uuid));
                bundled(player, "alerts.dangerous-building", event.builder.getPlayer().coloredName(), Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
            }
        }).subscribe();
    }

    public static void depositAlert(DepositEvent event) {
        if (!enabled() || !isDangerous(event.tile, event.tile.team, event.item)) return;

        var ids = StreamSupport.stream(event.player.team().data().players.spliterator(), false)
                .map(Player::uuid)
                .toList();

        getPlayersData(ids).doOnNext(data -> {
           if (data.alertsEnabled) {
               Player player = Groups.player.find(pl-> pl.uuid().equals(data.uuid));
               bundled(player, "alerts.dangerous-deposit", event.player.coloredName(), Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
           }
        }).subscribe();
    }

    private static boolean isDangerous(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    private static boolean isDangerous(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }

    private static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }
}
