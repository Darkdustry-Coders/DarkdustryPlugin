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
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.net.Packets.*;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Database.*;
import static darkdustry.components.MenuHandler.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Bundle.load();
        Config.load();
        Icons.load();
        MapParser.load();
        MenuHandler.load();

        Alerts.load();
        Effects.load();
        Ranks.load();
        SchemeSize.load();
        Translator.load();

        PluginEvents.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::packet);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;

        Timer.schedule(() -> Groups.player.each(player -> {
            var data = getPlayerData(player);
            data.playTime++;

            var rank = Ranks.getRank(data.rank);
            while (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                Ranks.setRank(player, rank = rank.next);
                data.rank = rank.id;

                showMenu(player, rankIncreaseMenu, "events.promotion.menu.header", "events.promotion.menu.content", new String[][] {{"ui.menus.close"}},
                        null, rank.localisedName(Find.locale(player.locale)), data.playTime, data.buildingsBuilt, data.gamesPlayed);
            }

            setPlayerData(data);
        }), 60f, 60f);

        // эта строчка исправляет обнаружение некоторых цветов
        Structs.each(color -> Colors.put(color, Color.white), "accent", "unlaunched", "highlight", "stat");
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
