package pandorum.events.listeners;

import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import pandorum.comp.Bundle;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.events.handlers.MenuHandler;
import pandorum.models.PlayerModel;

import static pandorum.Misc.*;
import static pandorum.PluginVars.*;

public class PlayerJoinListener {

    public static void call(final PlayerJoin event) {
        Ranks.updateName(event.player, name -> {
            event.player.name(name);
            Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
            sendToChat("events.player.join", name);
            BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.successColor).title(Strings.format("@ зашел на сервер.", Strings.stripColors(name))).build());
        });

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);

        updateTimers.put(event.player.uuid(), Timer.schedule(() -> {
            PlayerModel.find(event.player.uuid(), playerModel -> {
                playerModel.playTime += 1000;
                playerModel.save();
                Ranks.updateName(event.player);
            });
        }, 0f, 1f));

        PlayerModel.find(event.player.uuid(), playerModel -> playerModel.hellomsg, playerInfo -> Call.menu(event.player.con,
                MenuHandler.welcomeMenu,
                Bundle.format("events.welcome.header", findLocale(event.player.locale)),
                Bundle.format("events.welcome.message", findLocale(event.player.locale), discordServerUrl),
                new String[][] {{Bundle.format("events.welcome.ok", findLocale(event.player.locale))}, {Bundle.format("events.welcome.disable", findLocale(event.player.locale))}}
        ));

        bundled(event.player, "events.motd");
    }
}
