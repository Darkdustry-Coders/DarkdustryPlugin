package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Time;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.syncCooldownTime;
import static pandorum.util.PlayerUtils.bundled;

public class SyncCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (Time.timeSinceMillis(player.getInfo().lastSyncTime) < syncCooldownTime && !player.admin) {
            bundled(player, "корочcommands.sync.cooldown", syncCooldownTime / 1000);
            return;
        }

        player.getInfo().lastSyncTime = Time.millis();
        Call.worldDataBegin(player.con);
        netServer.sendWorldData(player);
    }
}
