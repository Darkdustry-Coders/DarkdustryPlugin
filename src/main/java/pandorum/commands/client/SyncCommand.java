package pandorum.commands.client;

import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.utils.Utils.bundled;
import static pandorum.PluginVars.syncCooldownTime;

public class SyncCommand {
    public static void run(final String[] args, final Player player) {
        if (Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * syncCooldownTime && !player.admin) {
            bundled(player, "commands.sync.time", syncCooldownTime);
            return;
        }

        player.getInfo().lastSyncTime = Time.millis();
        Call.worldDataBegin(player.con);
        netServer.sendWorldData(player);
    }
}
