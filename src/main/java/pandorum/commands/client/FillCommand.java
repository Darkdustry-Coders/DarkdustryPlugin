package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import pandorum.components.Icons;

import static mindustry.Vars.world;
import static pandorum.PluginVars.maxFillSize;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findBlock;

public class FillCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        if (!Strings.canParsePositiveInt(args[1]) || !Strings.canParsePositiveInt(args[2]) ||
            !Strings.canParsePositiveInt(args[3]) || !Strings.canParsePositiveInt(args[4])) {
            bundled(player, "commands.admin.fill.incorrect-number-format");
            return;
        }

        int 
                x1 = Strings.parseInt(args[1]), y1 = Strings.parseInt(args[2]),
                x2 = Strings.parseInt(args[3]), y2 = Strings.parseInt(args[4]),
                width = Math.abs(x1 - x2) + 1, height = Math.abs(y1 - y2) + 1;

        x1 = x1 < x2 ? x1 : x2;
        y1 = y1 < y2 ? y1 : y2;

        if (width * height > maxFillSize) {
            bundled(player, "commands.admin.fill.too-big-area", maxFillSize);
            return;
        }

        Block block = findBlock(args[0]);
        if (block == null) {
            bundled(player, "commands.block-not-found");
            return;
        }

        for (int x = x1; x < x1 + width; x += block.size) {
            for (int y = y1; y < y1 + width; y += block.size) {
                Tile tile = world.tile(x, y);
                if (tile == null) continue;
                else if (block.isFloor() && !block.isOverlay()) tile.setFloorNet(block, tile.overlay());
                else if (block.isOverlay()) tile.setFloorNet(tile.floor(), block);
                else tile.setNet(block, player.team(), (int) player.unit().rotation / 90);
            }
        }

        bundled(player, "commands.admin.fill.success", width, height, Icons.get(block.name, block.name));
    }
}
