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
import rewrite.commands.ClientCommands;
import rewrite.commands.DiscordCommands;
import rewrite.commands.ServerCommands;
import rewrite.components.*;
import rewrite.discord.Bot;
import rewrite.features.Alerts;
import rewrite.features.Effects;
import rewrite.features.Ranks;
import rewrite.features.Ranks.Rank;
import rewrite.features.Translator;
import rewrite.listeners.Filters;
import rewrite.listeners.NetHandlers;
import rewrite.listeners.PluginEvents;
import rewrite.utils.Find;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.get;
import static rewrite.components.Database.*;
import static rewrite.components.MenuHandler.rankIncreaseMenu;
import static rewrite.components.MenuHandler.showMenu;

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
        clientCommands = handler;
        for (ClientCommands command : ClientCommands.values())
            if (command.enabled()) handler.register(command.name(), get(command.params, ""), get(command.description, ""), command);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
        for (ServerCommands command : ServerCommands.values()) handler.register(command.name(), command.params, command.description, command);
    }

    public static void registerDiscordCommands(CommandHandler handler) {
        discordCommands = handler;
        for (DiscordCommands command : DiscordCommands.values())
            if (command.enabled()) handler.register(command.name(), command.params, command.description, command);
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
