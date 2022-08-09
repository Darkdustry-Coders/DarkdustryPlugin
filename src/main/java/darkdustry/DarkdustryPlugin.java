// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package darkdustry;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import darkdustry.commands.*;
import darkdustry.components.*;
import darkdustry.discord.Bot;
import darkdustry.discord.SlashCommands;
import darkdustry.features.*;
import darkdustry.features.Ranks.Rank;
import darkdustry.listeners.Filters;
import darkdustry.listeners.NetHandlers;
import darkdustry.listeners.PluginEvents;
import darkdustry.utils.Find;

import static mindustry.Vars.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.components.Database.*;
import static darkdustry.components.MenuHandler.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Effects.load();
        Alerts.load();
        Bundle.load();
        Config.load();
        Icons.load();
        Ranks.load();
        PluginEvents.load();
        MenuHandler.load();
        Translator.load();
        MapParser.load();

        Database.connect();
        Bot.connect();

        // TEST
        SlashCommands.load();
        SchemeSize.load();
        //Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::packet);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalidResponse;

        Timer.schedule(() -> Groups.player.each(player -> player.unit().moving(), Effects::onMove), 0f, 0.1f);
        Timer.schedule(() -> Groups.player.each(player -> {
            PlayerData data = getPlayerData(player);
            data.playTime++;

            Rank rank = Ranks.getRank(data.rank);
            if (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                Ranks.setRank(player, rank = rank.next);
                data.rank = rank.id;

                showMenu(player, rankIncreaseMenu, "events.rank-increase.menu.header", "events.rank-increase.menu.content", new String[][] {{"ui.menus.close"}},
                        null, rank.tag, rank.localisedName(Find.locale(player.locale)), data.playTime, data.buildingsBuilt, data.gamesPlayed);
            }

            setPlayerData(data);
        }), 0f, 60f);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        new ClientCommands(clientCommands = handler, defaultLocale);
        new AdminCommands(handler, defaultLocale);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        new ServerCommands(serverCommands = handler);
    }

    public static void registerDiscordCommands(CommandHandler handler) {
        new DiscordCommands(discordCommands = handler);
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
