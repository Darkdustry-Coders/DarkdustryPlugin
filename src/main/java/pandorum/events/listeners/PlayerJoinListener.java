package pandorum.events.listeners;

import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.net.Administration.Config;
import pandorum.components.Bundle;
import pandorum.components.Effects;
import pandorum.components.Ranks;
import pandorum.components.Ranks.Rank;
import pandorum.database.models.PlayerModel;
import pandorum.discord.Bot;
import pandorum.events.handlers.MenuHandler;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.util.Search.findLocale;

public class PlayerJoinListener {

    public static void call(final PlayerJoin event) {
        PlayerModel.find(event.player, playerInfo -> {
            Rank rank = Ranks.getRank(playerInfo.rank);
            String name = rank.tag + " [#" + event.player.color + "]" + event.player.getInfo().lastName;

            event.player.name(name);
            Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
            Utils.sendToChat("events.player.join", name);
            Bot.sendEmbed(Color.green, "@ присоединился к серверу.", Strings.stripColors(name));

            if (playerInfo.welcomeMessage) Call.menu(event.player.con,
                    MenuHandler.welcomeMenu,
                    Bundle.format("events.welcome.menu.header", findLocale(event.player.locale)),
                    Bundle.format("events.welcome.menu.message", findLocale(event.player.locale), Config.name.string(), discordServerUrl),
                    new String[][] {{Bundle.format("events.welcome.menu.close", findLocale(event.player.locale))}, {Bundle.format("events.welcome.menu.disable", findLocale(event.player.locale))}}
            );
        });

        Utils.bundled(event.player, "events.welcome.message", Config.name.string(), discordServerUrl);

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);
    }
}
