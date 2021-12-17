package pandorum.commands.client;

import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class SyncCommand {

    private static final float cooldownTime = 15f;

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
