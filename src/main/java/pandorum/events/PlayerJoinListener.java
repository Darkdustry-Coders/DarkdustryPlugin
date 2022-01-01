package pandorum.events;

import arc.util.Log;
import arc.util.Strings;
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

public class PlayerJoinListener {

    public static void call(final PlayerJoin event) {
        Ranks.updateRank(event.player, rank -> {
            event.player.name(Strings.format("@[#@]@", rank.tag, event.player.color.toString().toUpperCase(), event.player.getInfo().lastName));
            Log.info("@ зашёл на сервер, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());
            sendToChat("events.player.join", event.player.coloredName());
        });

        BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.successColor).title(Strings.format("@ зашел на сервер.", Strings.stripColors(event.player.getInfo().lastName))).build());
        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);

        PlayerModel.find(event.player.uuid(), playerInfo -> {
            if (playerInfo.hellomsg) {
                Call.menu(event.player.con,
                        MenuHandler.welcomeMenu,
                        Bundle.format("events.hellomsg.header", findLocale(event.player.locale)),
                        Bundle.format("events.hellomsg", findLocale(event.player.locale)),
                        new String[][] {{Bundle.format("events.hellomsg.ok", findLocale(event.player.locale))}, {Bundle.format("events.hellomsg.disable", findLocale(event.player.locale))}}
                );
            }
        });

        bundled(event.player, "events.motd");
    }
}
