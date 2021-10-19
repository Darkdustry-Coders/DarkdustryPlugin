package pandorum.commands.client;

import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.Misc;

import static pandorum.Misc.bundled;

public class CoreCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.permissionCheck(player, 5)) return;

        Block core = switch(args[0].toLowerCase()) {
            case "big", "nucleus" -> Blocks.coreNucleus;
            case "medium", "foundation" -> Blocks.coreFoundation;
            case "small", "shard" -> Blocks.coreShard;
            default -> null;
        };

        if (core == null) {
            bundled(player, "commands.admin.core.core-not-found");
            return;
        }
        Call.constructFinish(player.tileOn(), core, player.unit(), (byte)0, player.team(), false);
        bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
    }
}
