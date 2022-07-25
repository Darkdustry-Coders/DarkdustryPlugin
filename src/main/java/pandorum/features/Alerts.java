package pandorum.features;

import arc.func.Boolp;
import arc.func.Func;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class Alerts {

    // TODO вынести сюда все что связано с оповещениями и предупреждениями

    /** Фразы, которые будут отображаться игрокам в углу экрана. */
    public static Seq<Func<Player, String>> alertWords = new Seq<>();
    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();
    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }

    public static void load() {
        alertWords.add(player -> ""); // Пример

        dangerousBuildBlocks.put(Blocks.incinerator, () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor, Items.thorium);
    }

    public static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }

    public static boolean isDangerousBuild(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    public static boolean isDangerousDeposit(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }
}
