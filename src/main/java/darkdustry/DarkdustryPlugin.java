// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.graphics.Color;
import arc.graphics.Colors;
import arc.util.*;
import darkdustry.commands.AdminCommands;
import darkdustry.commands.ClientCommands;
import darkdustry.commands.ServerCommands;
import darkdustry.components.Config;
import darkdustry.components.Database;
import darkdustry.components.Icons;
import darkdustry.discord.Bot;
import darkdustry.features.Alerts;
import darkdustry.features.SchemeSize;
import darkdustry.features.menus.MenuHandler;
import darkdustry.listeners.Filters;
import darkdustry.listeners.NetHandlers;
import darkdustry.listeners.PluginEvents;
import mindustry.core.Version;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.net.Packets.*;
import useful.*;

import static arc.Core.app;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.updatePlayersData;
import static darkdustry.features.Ranks.updateRank;
import static darkdustry.features.menus.MenuHandler.showPromotionMenu;
import static darkdustry.utils.Utils.getAvailableMaps;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    public static void exit() {
        netServer.kickAll(KickReason.serverRestarting);
        app.post(Database::exit);
        app.post(Bot::exit);
        app.exit();
    }

    public static void info(String text, Object... values) {
        Log.infoTag("Darkdustry", Strings.format(text, values));
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", Strings.format(text, values));
    }

    @Override
    public void init() {
        info("Loading Darkdustry plugin.");
        Time.mark();

        Config.load();
        PluginEvents.load();

        AntiDdos.loadBlacklist();
        Bundle.load(DarkdustryPlugin.class, defaultLanguage);
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
        Icons.load();
        MenuHandler.load();
        SchemeSize.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::connect);
        net.handleServer(AdminRequestCallPacket.class, NetHandlers::adminRequest);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);

        netServer.invalidHandler = NetHandlers::invalidResponse;

        maps.setMapProvider((mode, map) -> getAvailableMaps().random(map));

        Timer.schedule(() -> updatePlayersData(Groups.player, (player, data) -> {
            data.playTime++;
            data.blocksPlaced += placedBlocksCache.remove(player.id);
            data.blocksBroken += brokenBlocksCache.remove(player.id);

            while (data.rank.checkNext(data.playTime, data.blocksPlaced, data.gamesPlayed, data.wavesSurvived)) {
                data.rank = data.rank.next;

                updateRank(player, data);
                showPromotionMenu(player, data);
            }
        }), 60f, 60f);

        // Исправляем обнаружение некоторых цветов
        Colors.getColors().putAll("accent", Color.white, "unlaunched", Color.white, "highlight", Color.white, "stat", Color.white, "negstat", Color.white);

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
