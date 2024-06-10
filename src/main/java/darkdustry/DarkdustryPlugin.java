// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.util.*;
import darkdustry.commands.*;
import darkdustry.config.*;
import darkdustry.database.*;
import darkdustry.database.models.ServerConfig;
import darkdustry.discord.*;
import darkdustry.features.*;
import darkdustry.features.menus.*;
import darkdustry.features.net.*;
import darkdustry.listeners.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Packets.*;
import useful.*;

import static darkdustry.config.Config.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Log.info("Loading Darkdustry plugin.");

        ModCommandHandler.load(); // ModCommandHandler must be loader before Commands, so
                                  // just in case we're loading it as soon as possible
        Time.mark();

        Console.load();
        Config.load();
        DiscordConfig.load();

        // AntiVpn.load(); // https://discord.com/channels/1149629218146230332/1208099245937393714/1208110284338626590
        Bundle.load(getClass());
        Commands.load();

        Alerts.load();
        MenuHandler.load();
        SchemeSize.load();
        Spectate.init();

        Database.connect();
        Socket.connect();

        PolymerAI.load();

        PluginEvents.load();
        SocketEvents.load();

        ServerConfig.get();

        if (config.mode.isMainServer) {
            DiscordBot.connect();
            DiscordCommands.load();
        }

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::connect);
        net.handleServer(AdminRequestCallPacket.class, NetHandlers::adminRequest);

        netServer.admins.addChatFilter(NetHandlers::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;

        maps.setMapProvider((mode, map) -> availableMaps().random(map));

        Log.info("Darkdustry plugin loaded in @ ms.", Time.elapsed());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommands.load();
        AdminCommands.load();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommands.load(handler);
    }
}
