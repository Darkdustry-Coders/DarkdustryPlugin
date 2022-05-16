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
import pandorum.listeners.handlers.MenuHandler;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.PluginVars.datas;
import static pandorum.PluginVars.discordServerUrl;
import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;
import static pandorum.util.Search.findLocale;

public class OnPlayerJoin implements Cons<PlayerJoin> {

    public void get(PlayerJoin event) {
        PlayerData data = getPlayerData(event.player.uuid());
        if (data == null) {
            data = new PlayerData();
            setPlayerData(event.player.uuid(), data);
        }

        String name = data.rank.tag + "[#" + event.player.color + "]" + event.player.getInfo().lastName;
        event.player.name(name);

        Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
        Utils.sendToChat("events.player.join", name);
        Bot.sendEmbed(Color.green, "@ зашел на сервер.", Strings.stripColors(name));

        if (data.welcomeMessage) Call.menu(event.player.con,
                MenuHandler.welcomeMenu,
                Bundle.format("events.welcome.menu.header", findLocale(event.player.locale)),
                Bundle.format("events.welcome.menu.content", findLocale(event.player.locale), Config.name.string(), discordServerUrl),
                new String[][] {{Bundle.format("ui.menus.close", findLocale(event.player.locale))}, {Bundle.format("events.welcome.menu.disable", findLocale(event.player.locale))}}
        );

        datas.put(event.player.uuid(), data);

        Utils.bundled(event.player, "events.welcome.message", Config.name.string(), discordServerUrl);

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);
    }
}
