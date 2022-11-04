// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.graphics.*;
import arc.util.*;
import darkdustry.commands.*;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.*;
import darkdustry.listeners.*;
import darkdustry.utils.Find;
import mindustry.core.Version;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.net.Packets.*;
import useful.*;

import static arc.Core.app;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.getPlayersData;
import static darkdustry.features.Ranks.updateRank;
import static darkdustry.features.menus.MenuHandler.showMenuClose;
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

    public static void discord(String text, Object... values) {
        Log.infoTag("Discord", Strings.format(text, values));
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", Strings.format(text, values));
    }

    @Override
    public void init() {
        info("Loading Darkdustry plugin.");
        Time.mark();

        PluginEvents.load();

        Bundle.load(DarkdustryPlugin.class);
        DynamicMenus.load();

        Config.load();
        Console.load();
        Icons.load();
        MapParser.load();
        Translator.load();

        Alerts.load();
        Effects.load();
        SchemeSize.load();
        Scripts.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::connect);
        net.handleServer(AdminRequestCallPacket.class, NetHandlers::adminRequest);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);

        netServer.invalidHandler = NetHandlers::invalidResponse;

        maps.setMapProvider((mode, map) -> getAvailableMaps().select(mode::valid).random(map));

        Timer.schedule(() -> {
            if (Groups.player.isEmpty()) return;

            getPlayersData(Groups.player).doOnNext(data -> {
                var player = Find.playerByUuid(data.uuid);
                if (player == null) return;

                data.playTime++;

                while (data.rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                    data.rank = data.rank.next;
                    updateRank(player, data);

                    showMenuClose(player, "events.promotion.header", "events.promotion.content", data.rank.localisedName(player), data.playTime, data.buildingsBuilt, data.gamesPlayed);
                }
            }).collectList().flatMap(Database::setPlayersData).subscribe();
        }, 60, 60);

        // эта строчка исправляет обнаружение некоторых цветов
        Structs.each(color -> Colors.put(color, Color.white), "accent", "unlaunched", "highlight", "stat");

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