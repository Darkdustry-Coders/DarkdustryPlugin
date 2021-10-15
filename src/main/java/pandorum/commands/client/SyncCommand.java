package pandorum.commands.client;

import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class SyncCommand {
    public static void run(final String[] args, final Player player) {
        if (Time.timeSinceMillis(player.getInfo().lastSyncTime) < 1000 * 15) {
            bundled(player, "commands.sync.time");
            return;
        }

        player.getInfo().lastSyncTime = Time.millis();
        Call.worldDataBegin(player.con);
        netServer.sendWorldData(player);
    }
}
