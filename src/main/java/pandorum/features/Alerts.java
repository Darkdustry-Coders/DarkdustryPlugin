package pandorum.features;

import arc.func.Boolp;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.util.Interval;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.components.Icons;
import pandorum.data.PlayerData;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.PlayerUtils.bundled;

public class Alerts {

    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();
    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static final Interval alertsInterval = new Interval();

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }

    public static void load() {
        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (!enabled()) return;

        if (isDangerousBuild(event.builder.buildPlan().block, event.team, event.tile) && alertsInterval.get(alertsTimer * 60f)) {
            event.team.data().players.each(player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alerts.dangerous-building", event.builder.getPlayer().name, Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
                }
            });
        }
    }

    public static void depositAlert(DepositEvent event) {
        if (!enabled()) return;

        if (isDangerousDeposit(event.tile, event.tile.team, event.item)) {
            event.player.team().data().players.each(player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alerts.dangerous-deposit", event.player.name, Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
                }
            });
        }
    }

    public static boolean isDangerousBuild(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    public static boolean isDangerousDeposit(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }

    public static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }
}
