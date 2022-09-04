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

import java.util.stream.StreamSupport;

import static arc.Core.app;
import static darkdustry.PluginVars.*;
import static darkdustry.components.MenuHandler.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    public static void exit() {
        netServer.kickAll(KickReason.serverRestarting);
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

        Bundle.load();
        Config.load();
        Icons.load();
        MapParser.load();
        MenuHandler.load();

        Alerts.load();
        Effects.load();
        Ranks.load();
        SchemeSize.load();
        Scripts.load();
        Translator.load();

        PluginEvents.load();
        MongoDB.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::packet);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;

        Timer.schedule(() -> {
            if (Groups.player.size() == 0) return;
            var ids = StreamSupport.stream(Groups.player.spliterator(), false)
                    .map(Player::uuid)
                    .toList();

            MongoDB.getPlayersData(ids).doOnNext(data -> {
                Player player = Groups.player.find(pl -> pl.uuid().equals(data.uuid));
                if (player != null) {
                    data.playTime++;
                    var rank = Ranks.getRank(data.rank);
                    while (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                        Ranks.setRank(player, rank = rank.next);
                        data.rank = rank.id;
                        showMenu(player, rankIncreaseMenu, "events.promotion.menu.header", "events.promotion.menu.content", new String[][] {{"ui.menus.close"}},
                                null, rank.localisedName(Find.locale(player.locale)), data.playTime, data.buildingsBuilt, data.gamesPlayed);
                    }
                }
            }).collectList().flatMap(MongoDB::setPlayersData).subscribe();
        }, 60f, 60f);

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
