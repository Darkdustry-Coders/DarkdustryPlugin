package pandorum.commands.client;

import mindustry.content.Blocks;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import pandorum.Misc;

import static pandorum.Misc.bundled;

public class CoreCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;

        Block core = args.length > 0 ? switch (args[0].toLowerCase()) {
            case "big", "nucleus" -> Blocks.coreNucleus;
            case "medium", "foundation" -> Blocks.coreFoundation;
            case "small", "shard" -> Blocks.coreShard;
            default -> null
        } : Blocks.coreShard;

        if (core == null) {
            bundled(player, "commands.admin.core.core-not-found");
            return;
        }

        try {
            Call.constructFinish(player.tileOn(), core, player.unit(), (byte) 0, player.team(), false);
            bundled(player, player.tileOn().block() == core ? "commands.admin.core.success" : "commands.admin.core.failed");
        } catch (Exception e) {
            bundled(player, "commands.admin.core.failed");
        }
    }
}
