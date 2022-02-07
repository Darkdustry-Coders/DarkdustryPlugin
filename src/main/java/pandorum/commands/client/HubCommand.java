package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.net;
import static mindustry.Vars.port;
import static pandorum.PluginVars.config;
import static pandorum.util.Utils.bundled;

public class HubCommand {
    public static void run(final String[] args, final Player player) {
        net.pingHost(config.hubIp, port, host -> Call.connect(player.con, host.address, host.port), e -> bundled(player, "commands.hub.offline"));
    }
}
