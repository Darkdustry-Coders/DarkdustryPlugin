// Rewrites are always better.
// (C) Skat, 2021 год до н. э.

package rewrite;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.core.Version;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.ConnectPacket;
import rewrite.commands.*;
import rewrite.components.*;
import rewrite.discord.Bot;
import rewrite.features.*;
import rewrite.features.Ranks.Rank;
import rewrite.listeners.*;
import rewrite.utils.Find;

import static arc.Core.*;
import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;
import static rewrite.components.MenuHandler.*;

@SuppressWarnings("unused")
public class DarkdustryPlugin extends Plugin {

    @Override
    public void init() {
        Effects.load();
        Alerts.load();
        Config.load();
        Bundle.load();
        Icons.load();
        Ranks.load();
        PluginEvents.load();
        MenuHandler.load();
        Translator.load();
        MapParser.load();

        Database.connect();
        Bot.connect();

        Version.build = -1;

        net.handleServer(Connect.class, NetHandlers::connect);
        net.handleServer(ConnectPacket.class, NetHandlers::packet);

        netServer.admins.addActionFilter(Filters::action);
        netServer.admins.addChatFilter(Filters::chat);
        netServer.invalidHandler = NetHandlers::invalide;

        Timer.schedule(() -> Groups.player.each(player -> player.unit().moving(), Effects::onMove), 0f, 0.1f);
        Timer.schedule(() -> Groups.player.each(player -> {
            PlayerData data = getPlayerData(player);
            data.playTime++;

            Rank rank = Ranks.getRank(data.rank);
            if (rank.checkNext(data.playTime, data.buildingsBuilt, data.gamesPlayed)) {
                Ranks.setRank(player, rank = rank.next);
                data.rank = rank.id;

                showMenu(player, rankIncreaseMenu, "events.rank-increase.menu.header", "events.rank-increase.menu.content", new String[][] { { "ui.menus.close" } },
                        null, rank.tag, get(rank.name, Find.locale(player.locale)), data.playTime, data.buildingsBuilt, data.gamesPlayed);
            }

            setPlayerData(data);
        }), 0f, 60f);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        app.post(() -> new ClientCommands(clientCommands = handler, defaultLocale));
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        app.post(() -> new ServerCommands(serverCommands = handler, consoleLocale));
    }

    public static void registerDiscordCommands(CommandHandler handler) {
        app.post(() -> new DiscordCommands(discordCommands = handler, consoleLocale));
    }

    public static void info(String text) {
        Log.infoTag("Darkdustry", text);
    }

    public static void info(String text, Object... values) {
        Log.infoTag("Darkdustry", Strings.format(text, values));
    }

    public static void disc(String text, Object... values) {
        Log.infoTag("Discord", Strings.format(text, values));
    }

    public static void error(String text) {
        Log.errTag("Darkdustry", text);
    }

    public static void error(String text, Object... values) {
        Log.errTag("Darkdustry", Strings.format(text, values));
    }
}
