package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.net;
import static mindustry.Vars.port;
import static pandorum.PluginVars.config;
import static pandorum.util.PlayerUtils.bundled;

public class HubCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        String[] address = config.hubIp.split(":");
        String hubIp = address[0];
        int hubPort = address.length > 1 ? Strings.parseInt(address[1], port) : port;
        net.pingHost(hubIp, hubPort, host -> Call.connect(player.con, host.address, host.port), e -> bundled(player, "commands.hub.offline"));
    }
}
