// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.graphics.Color;
import arc.graphics.Colors;
import arc.util.*;
import darkdustry.commands.AdminCommands;
import darkdustry.commands.ClientCommands;
import darkdustry.commands.ServerCommands;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.features.*;
import darkdustry.listeners.Filters;
import darkdustry.listeners.NetHandlers;
import darkdustry.listeners.PluginEvents;
import darkdustry.utils.Find;
import mindustry.core.Version;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import reactor.core.publisher.Mono;

import java.util.stream.StreamSupport;

import static darkdustry.PluginVars.clientCommands;
import static darkdustry.PluginVars.serverCommands;
import static darkdustry.components.Database.getPlayerData;
import static darkdustry.components.Database.setPlayerData;
import static darkdustry.components.MenuHandler.rankIncreaseMenu;
import static darkdustry.components.MenuHandler.showMenu;
import static darkdustry.components.MongoDB.*;
import static mindustry.Vars.net;
import static mindustry.Vars.netServer;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

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
        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::packet);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;


        Timer.schedule(() -> {
            var ids = StreamSupport.stream(Groups.player.spliterator(), false)
                    .map(Player::uuid)
                    .toList();
            MongoDB.getPlayersDataAsync(ids).doOnNext(data->{
                Player player = Groups.player.find(pl-> pl.uuid().equals(data.uuid));
                if(player!=null){
                    data.playTime++;
                    var rank = Ranks.getRank(data.rank);
                    while (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                        Ranks.setRank(player, rank = rank.next);
                        data.rank = rank.id;
                        showMenu(player, rankIncreaseMenu, "events.promotion.menu.header", "events.promotion.menu.content", new String[][]{{"ui.menus.close"}},
                                null, rank.localisedName(Find.locale(player.locale)), data.playTime, data.buildingsBuilt, data.gamesPlayed);
                    }
                }
            }).collectList()
                    .flatMap(MongoDB::setPlayerDatas).subscribe();
        }, 60f, 60f);

        // эта строчка исправляет обнаружение некоторых цветов
        Structs.each(color -> Colors.put(color, Color.white), "accent", "unlaunched", "highlight", "stat");

        info("Darkdustry plugin loaded in @ ms.", Time.elapsed());
        info("You can update plugin with the command update.");
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

    public static void info(String text, Object... values) {
        Log.infoTag("Darkdustry", Strings.format(text, values));
    }

    public static void discord(String text, Object... values) {
        Log.infoTag("Discord", Strings.format(text, values));
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", Strings.format(text, values));
    }
}
