package pandorum.commands.client;

import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.OverrideCommand;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class SyncCommand {

    private static final int cooldownTime = 15;
    @OverrideCommand
    @ClientCommand(name = "sync", args = "", description = "Re-synchronize world state.")
    public static void run(final String[] args, final Player player) {
        if (Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * cooldownTime) {
            bundled(player, "commands.sync.time", cooldownTime);
            return;
        }

        player.getInfo().lastSyncTime = Time.millis();
        Call.worldDataBegin(player.con);
        netServer.sendWorldData(player);
    }
}
