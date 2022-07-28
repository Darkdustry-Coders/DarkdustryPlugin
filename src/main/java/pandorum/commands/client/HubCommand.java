package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static mindustry.Vars.net;
import static pandorum.PluginVars.config;
import static pandorum.util.PlayerUtils.bundled;

public class HubCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        net.pingHost(config.hubIp, config.hubPort, host -> Call.connect(player.con, host.address, host.port), e -> bundled(player, "commands.hub.offline"));
    }
}
