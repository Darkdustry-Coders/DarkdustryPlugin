package pandorum.commands.client;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.TreeBlock;
import pandorum.Misc;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.Misc.bundled;

public class FillCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1]) || Strings.parseInt(args[0]) > 50 || Strings.parseInt(args[1]) > 50) {
            bundled(player, "commands.admin.fill.incorrect-number-format");
            return;
        }

        int w = Mathf.clamp(Strings.parseInt(args[0]), 0, 50) + player.tileX();
        int h = Mathf.clamp(Strings.parseInt(args[1]), 0, 50) + player.tileY();

        Floor floor = (Floor)content.blocks().find(b -> b.isFloor() && b.name.equalsIgnoreCase(args[2]));
        Block block = args.length > 3 ? content.blocks().find(o -> (o.isOverlay() || o instanceof OreBlock || o instanceof Prop || o instanceof TreeBlock) && o.name.equalsIgnoreCase(args[3])) : Blocks.air;
        if (floor == null || block == null) {
            bundled(player, "commands.admin.fill.incorrect-type");
            return;
        }

        for (int x = player.tileX(); x < w; x++) {
            for (int y = player.tileY(); y < h; y++) {
                if (world.tile(x, y) != null) {
                    world.tile(x, y).setFloorNet(floor);
                    world.tile(x, y).setNet(block);
                }
            }
        }
        bundled(player, "commands.admin.fill.success", floor, block);
    }
}
