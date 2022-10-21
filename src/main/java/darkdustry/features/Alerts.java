package darkdustry.features;

import arc.func.Boolp;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.util.Interval;
import darkdustry.components.Icons;
import darkdustry.utils.Find;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.Item;
import mindustry.world.*;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.components.MongoDB.getPlayersData;
import static mindustry.Vars.state;

public class Alerts {

    public static final Interval alertsInterval = new Interval();

    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();

    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static boolean enabled() {
        return config.mode.isDefault();
    }

    public static void load() {
        if (!enabled()) return;

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (!enabled() || !isDangerous(event.builder.buildPlan().block, event.team, event.tile) || !alertsInterval.get(60f * alertsTimer))
            return;

        var block = event.builder.buildPlan().block;

        getPlayersData(event.team.data().players.map(Player::uuid)).doOnNext(data -> {
            var player = Find.playerByUuid(data.uuid);
            if (player != null && data.alerts)
                bundled(player, "alerts.dangerous-building", event.builder.getPlayer().coloredName(), Icons.get(block), event.tile.x, event.tile.y);
        }).subscribe();
    }

    public static void depositAlert(DepositEvent event) {
        if (!enabled() || !isDangerous(event.tile, event.tile.team, event.item)) return;

        var block = event.tile.block;
        var item = event.item;

        getPlayersData(event.player.team().data().players.map(Player::uuid)).doOnNext(data -> {
            var player = Find.playerByUuid(data.uuid);
            if (player != null && data.alerts)
                bundled(player, "alerts.dangerous-deposit", event.player.coloredName(), Icons.get(item), Icons.get(block), event.tile.tileX(), event.tile.tileY());
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