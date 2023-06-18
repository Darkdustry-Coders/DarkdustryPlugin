// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.util.*;
import darkdustry.commands.*;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.*;
import darkdustry.features.menus.MenuHandler;
import darkdustry.listeners.*;
import mindustry.core.Version;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.net.Packets.*;
import useful.*;

import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    public static void info(String text, Object... values) {
        Log.infoTag("Darkdustry", format(text, values));
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", format(text, values));
    }

    @Override
    public void init() {
        info("Loading Darkdustry plugin.");
        Time.mark();

        Config.load();
        Console.load();
        PluginEvents.load();

        AntiDdos.loadBlacklist();
        Bundle.load(getClass());
        Cooldowns.defaults(
                "default", 1000L,
                "sync", 15000L,
                "votekick", 300000L,
                "login", 900000L,
                "rtv", 30000L,
                "vnw", 30000L,
                "savemap", 90000L,
                "loadsave", 90000L
        );

        Alerts.load();
        MenuHandler.load();
        SchemeSize.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::connect);
        net.handleServer(AdminRequestCallPacket.class, NetHandlers::adminRequest);

        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;

        maps.setMapProvider((mode, map) -> availableMaps().random(map));

        Timer.schedule(() -> Groups.player.each(player -> {
            var data = Cache.get(player);
            data.playTime++;

            while (data.rank.checkNext(data.playTime, data.blocksPlaced, data.gamesPlayed, data.wavesSurvived)) {
                data.rank = data.rank.next;

                Ranks.name(player, data);
                MenuHandler.showPromotionMenu(player, data);
            }

            savePlayerData(data);
        }), 60f, 60f);

        info("Darkdustry plugin loaded in @ ms.", Time.elapsed());
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