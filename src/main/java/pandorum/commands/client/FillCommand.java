package pandorum.commands.client;

import arc.math.Mathf;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.components.Icons;
import pandorum.util.Utils;

import static mindustry.Vars.world;
import static pandorum.PluginVars.maxFillSize;
import static pandorum.util.Search.findBlock;
import static pandorum.util.Utils.bundled;

public class FillCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!Utils.isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1])) {
            bundled(player, "commands.admin.fill.incorrect-number-format");
            return;
        }

        int width = Strings.parseInt(args[0]), height = Strings.parseInt(args[1]);
        if (width * height > maxFillSize) {
            bundled(player, "commands.admin.fill.too-big-area", maxFillSize);
            return;
        }

        Block block = findBlock(args[2]);
        if (block == null) {
            bundled(player, "commands.block-not-found");
            return;
        }

        for (int x = player.tileX(); x < width + player.tileX(); x += block.size) {
            for (int y = player.tileY(); y < height + player.tileY(); y += block.size) {
                Tile tile = world.tile(x, y);
                if (tile != null) {
                    if (block.isFloor()) tile.setFloorNet(block, tile.overlay());
                    else if (block.isOverlay()) tile.setFloorNet(tile.floor(), block);
                    else tile.setNet(block, player.team(), Mathf.random(0, 3));
                }
            }
        }

        bundled(player, "commands.admin.fill.success", width, height, Icons.get(block.name, block.name));
    }
}
