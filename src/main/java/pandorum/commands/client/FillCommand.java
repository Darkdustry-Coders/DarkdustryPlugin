package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.components.Icons;

import static mindustry.Vars.world;
import static pandorum.PluginVars.maxFillSize;
import static pandorum.util.Search.findBlock;
import static pandorum.util.Utils.bundled;

public class FillCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1]) || Strings.parseInt(args[0]) > maxFillSize || Strings.parseInt(args[1]) > maxFillSize) {
            bundled(player, "commands.admin.fill.incorrect-number-format", maxFillSize);
            return;
        }

        int width = Strings.parseInt(args[0]), height = Strings.parseInt(args[1]);

        Block block = findBlock(args[2]);
        if (block == null) {
            bundled(player, "commands.block-not-found");
            return;
        }

        for (int x = player.tileX(); x < width + player.tileX(); x += block.size) {
            for (int y = player.tileY(); y < height + player.tileY(); y += block.size) {
                Tile tile = world.tile(x, y);
                if (tile != null) {
                    if (block.isFloor()) tile.setFloorNet(block);
                    else if (block.isOverlay()) tile.setFloorNet(Blocks.air, block);
                    else tile.setNet(block, player.team(), Mathf.random(0, 3));
                }
            }
        }

        bundled(player, "commands.admin.fill.success", width, height, Icons.get(block.name, block.name));
    }
}
