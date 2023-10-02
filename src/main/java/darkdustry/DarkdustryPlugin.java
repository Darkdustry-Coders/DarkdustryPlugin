// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.util.*;
import darkdustry.commands.*;
import darkdustry.config.Config;
import darkdustry.database.Database;
import darkdustry.config.DiscordConfig;
import darkdustry.features.*;
import darkdustry.features.menus.MenuHandler;
import darkdustry.features.net.Socket;
import darkdustry.listeners.*;
import mindustry.core.Version;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.net.Packets.*;
import useful.*;

import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Log.info("Loading Darkdustry plugin.");
        Time.mark();

        Console.load();
        Config.load();
        DiscordConfig.load();

        AntiVpn.load();
        Bundle.load(getClass());
        Cooldowns.defaults(
                "sync", 15000L,
                "votekick", 300000L,
                "login", 900000L,
                "rtv", 60000L,
                "vnw", 60000L,
                "votesave", 180000L,
                "voteload", 180000L
        );

        Alerts.load();
        MenuHandler.load();
        SchemeSize.load();

        Database.connect();
        Socket.connect();

        PluginEvents.load();
        SocketEvents.load();

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
        clientCommands = handler;
        ClientCommands.load();
        AdminCommands.load();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
        ServerCommands.load();
    }
}