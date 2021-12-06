package pandorum.commands.client;

import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.gamemodes.RequireGamemode;
import pandorum.comp.Config;

import static mindustry.Vars.content;
import static mindustry.Vars.world;
import static pandorum.Misc.bundled;

public class FillCommand {
    private static final int maxSize = 25;

    @RequireGamemode(Gamemode = Config.Gamemode.sandbox)
    @ClientCommand(name = "fill", args = "<width> <height> <block_1> [block_2]", description = "Fill an area with some floor.", admin = true)
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        if (!Strings.canParsePositiveInt(args[0]) || !Strings.canParsePositiveInt(args[1]) || Strings.parseInt(args[0]) > maxSize || Strings.parseInt(args[1]) > maxSize) {
            bundled(player, "commands.admin.fill.incorrect-number-format", maxSize);
            return;
        }

        int w = Strings.parseInt(args[0]) + player.tileX();
        int h = Strings.parseInt(args[1]) + player.tileY();

        Floor floor;
        Block block;

        if (args.length == 3) {
            Block found = content.blocks().find(b -> b.name.equalsIgnoreCase(args[2]));
            if (found.isFloor()) {
                floor = found.asFloor();
                block = Blocks.air;
            } else {
                floor = Blocks.air.asFloor();
                block = found;
            }
        } else {
            floor = content.blocks().find(b -> b.isFloor() && b.name.equalsIgnoreCase(args[2])).asFloor();
            block = content.blocks().find(b -> b.name.equalsIgnoreCase(args[3]));
        }

        if (floor == null || block == null) {
            bundled(player, "commands.admin.fill.incorrect-type");
            return;
        }

        for (int x = player.tileX(); x < w; x++) {
            for (int y = player.tileY(); y < h; y++) {
                if (world.tile(x, y) != null) world.tile(x, y).setFloorNet(floor);
            }
        }

        for (int x = player.tileX(); x < w; x += block.size) {
            for (int y = player.tileY(); y < h; y += block.size) {
                if (world.tile(x, y) != null) world.tile(x, y).setNet(block);
            }
        }

        bundled(player, "commands.admin.fill.success");
    }
}
