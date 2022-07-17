package pandorum.listeners.events;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.discord.Bot;
import pandorum.features.Effects;
import pandorum.features.Ranks;

import java.awt.*;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.data.Database.getPlayerData;
import static pandorum.discord.Bot.botChannel;
import static pandorum.listeners.handlers.MenuHandler.welcomeMenu;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.sendToChat;
import static pandorum.util.Search.findLocale;

public class OnPlayerJoin implements Cons<PlayerJoin> {

    public void get(PlayerJoin event) {
        PlayerData data = getPlayerData(event.player.uuid());

        String name = Ranks.getRank(data.rank).tag + "[#" + event.player.color + "]" + event.player.getInfo().lastName;
        event.player.name(name);

        Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
        sendToChat("events.player.join", name);
        Bot.sendEmbed(botChannel, Color.green, "@ присоединился", Strings.stripColors(name));

        if (data.welcomeMessage) Call.menu(event.player.con, welcomeMenu,
                Bundle.format("welcome.menu.header", findLocale(event.player.locale)),
                Bundle.format("welcome.menu.content", findLocale(event.player.locale), Config.serverName.string(), discordServerUrl),
                new String[][] {{Bundle.format("ui.menus.close", findLocale(event.player.locale))}, {Bundle.format("welcome.menu.disable", findLocale(event.player.locale))}}
        );

        bundled(event.player, "welcome.message", Config.serverName.string(), discordServerUrl);

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);

        Seq<Player> players = Groups.player.copy(new Seq<>());
        Bot.updateBotStatus(players.size);
    }
}
