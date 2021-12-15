package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.PandorumPlugin;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.gamemodes.DisableGamemode;
import pandorum.comp.Config;
import pandorum.struct.Tuple2;

import static mindustry.Vars.net;
import static pandorum.Misc.bundled;

public class HubCommand {
    @DisableGamemode(Gamemode = Config.Gamemode.hub)
    @ClientCommand(name = "hub", args = "", description = "Connect to HUB.")
    public static void run(final String[] args, final Player player) {
        Tuple2<String, Integer> hub = PandorumPlugin.config.hubIp();
        net.pingHost(hub.t1, hub.t2, host -> Call.connect(player.con, hub.t1, hub.t2), e -> bundled(player, "commands.hub.offline"));
    }
}
