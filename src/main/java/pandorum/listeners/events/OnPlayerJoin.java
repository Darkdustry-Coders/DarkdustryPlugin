package pandorum.listeners.events;

import arc.func.Cons;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.net.Administration.Config;
import pandorum.components.Bundle;
import pandorum.data.PlayerData;
import pandorum.discord.Bot;
import pandorum.features.Effects;
import pandorum.features.Ranks;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.data.Database.getPlayerData;
import static pandorum.listeners.handlers.MenuHandler.welcomeMenu;
import static pandorum.util.Search.findLocale;

public class OnPlayerJoin implements Cons<PlayerJoin> {

    public void get(PlayerJoin event) {
        PlayerData data = getPlayerData(event.player.uuid());

        String name = Ranks.getRank(data.rank).tag + "[#" + event.player.color + "]" + event.player.getInfo().lastName;
        event.player.name(name);

        Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
        Utils.sendToChat("events.player.join", name);
        Bot.sendEmbed(Color.green, "@ зашел на сервер.", Strings.stripColors(name));

        if (data.welcomeMessage) Call.menu(event.player.con, welcomeMenu,
                Bundle.format("events.welcome.menu.header", findLocale(event.player.locale)),
                Bundle.format("events.welcome.menu.content", findLocale(event.player.locale), Config.name.string(), discordServerUrl),
                new String[][] {{Bundle.format("ui.menus.close", findLocale(event.player.locale))}, {Bundle.format("events.welcome.menu.disable", findLocale(event.player.locale))}}
        );

        Utils.bundled(event.player, "events.welcome.message", Config.name.string(), discordServerUrl);

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);
    }
}
