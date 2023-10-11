package darkdustry.features;

import arc.func.Boolp;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.util.Interval;
import darkdustry.database.Cache;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.*;
import useful.Bundle;

import static darkdustry.PluginVars.*;
import static darkdustry.config.Config.*;
import static mindustry.Vars.*;

public class Alerts {

    public static final Interval alertsInterval = new Interval();

    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();

    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static boolean disabled() {
        return !config.mode.isDefault;
    }

    public static void load() {
        if (disabled()) return;

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (disabled()) return;

        if (isDangerousBlock(event.builder.buildPlan().block, event.team, event.tile) && alertsInterval.get(alertsTimer * 60f))
            Bundle.send(player -> Cache.get(player).alerts, "alerts.dangerous-building", event.builder.getPlayer().coloredName(), event.builder.buildPlan().block.emoji(), event.tile.x, event.tile.y);
    }

    public static void depositAlert(DepositEvent event) {
        if (disabled()) return;

        if (isDangerousDeposit(event.tile, event.tile.team, event.item))
            Bundle.send(player -> Cache.get(player).alerts, "alerts.dangerous-deposit", event.player.coloredName(), event.item.emoji(), event.tile.block.emoji(), event.tile.tileX(), event.tile.tileY());
    }

    private static boolean isDangerousBlock(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    private static boolean isDangerousDeposit(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }

    private static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }
}