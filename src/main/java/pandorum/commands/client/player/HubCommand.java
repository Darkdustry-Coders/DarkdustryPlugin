package pandorum.commands.client.player;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.struct.Tuple2;

import static mindustry.Vars.net;
import static pandorum.Misc.bundled;

public class HubCommand {
    public static void run(final String[] args, final Player player) {
        Tuple2<String, Integer> hub = PandorumPlugin.config.hubIp();
        net.pingHost(hub.t1, hub.t2, host -> Call.connect(player.con, hub.t1, hub.t2), e -> bundled(player, "commands.hub.offline"));
    }
}
