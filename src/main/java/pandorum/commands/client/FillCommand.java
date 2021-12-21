package pandorum.commands.client;

import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.Misc.bundled;
import static pandorum.Misc.findBlock;

public class FillCommand {

    private static final int maxSize = 25;

    public static void run(final String[] args, final Player player) {
        if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1]) || Strings.parseInt(args[0]) > maxSize || Strings.parseInt(args[1]) > maxSize) {
            bundled(player, "commands.admin.fill.incorrect-number-format", maxSize);
            return;
        }

        int w = Strings.parseInt(args[0]) + player.tileX();
        int h = Strings.parseInt(args[1]) + player.tileY();

        Block block = findBlock(args[0]);
        if (block == null) {
            bundled(player, "commands.admin.fill.block-not-found");
            return;
        }

        for (int x = player.tileX(); x < w; x += block.size) {
            for (int y = player.tileY(); y < h; y += block.size) {
                Tile tile = world.tile(x, y);
                if (tile != null) {
                    if (block.isFloor()) tile.setFloorNet(block);
                    else tile.setNet(block, player.team(), 0);
                }
            }
        }

        bundled(player, "commands.admin.fill.success");
    }
}
