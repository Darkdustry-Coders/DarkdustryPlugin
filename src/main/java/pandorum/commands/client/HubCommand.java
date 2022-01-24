package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import reactor.util.function.Tuple2;

import static mindustry.Vars.net;
import static pandorum.Misc.bundled;
import static pandorum.Misc.hubIp;

public class HubCommand {
    public static void run(final String[] args, final Player player) {
        Tuple2<String, Integer> hub = hubIp();
        net.pingHost(hub.getT1(), hub.getT2(), host -> Call.connect(player.con, hub.getT1(), hub.getT2()), e -> bundled(player, "commands.hub.offline"));
    }
}
