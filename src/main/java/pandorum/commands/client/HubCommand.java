package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.struct.Tuple2;

import static mindustry.Vars.net;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.hubIp;

public class HubCommand {
    public static void run(final String[] args, final Player player) {
        Tuple2<String, Integer> hub = hubIp();
        net.pingHost(hub.t1, hub.t2, host -> Call.connect(player.con, hub.t1, hub.t2), e -> bundled(player, "commands.hub.offline"));
    }
}
