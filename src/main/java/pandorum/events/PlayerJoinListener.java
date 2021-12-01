package pandorum.events;

import arc.util.Log;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import mindustry.gen.Call;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.events.handlers.MenuHandler;
import pandorum.models.PlayerModel;

import static pandorum.Misc.*;

public class PlayerJoinListener {
    public static void call(final EventType.PlayerJoin event) {
        Ranks.updateRank(event.player, rank -> event.player.name(rank.tag + "[#" + event.player.color.toString().toUpperCase() + "]" + event.player.getInfo().lastName));

        Log.info("@ зашёл на сервер, IP: @, ID: @", event.player.getInfo().lastName, event.player.ip(), event.player.uuid());
        sendToChat("events.player-join", event.player.color.toString().toUpperCase(), event.player.getInfo().lastName);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.successColor)
                .title(Strings.format("@ зашел на сервер.", Strings.stripColors(event.player.getInfo().lastName)))
                .build();

        BotHandler.sendEmbed(embed);

        Effects.onJoin(event.player);

        PlayerModel.find(new BasicDBObject("UUID", event.player.uuid()), playerInfo -> {
            if (playerInfo.hellomsg) {
                Call.menu(event.player.con,
                        MenuHandler.welcomeMenu,
                        Bundle.format("events.hellomsg.header", findLocale(event.player.locale)),
                        Bundle.format("events.hellomsg", findLocale(event.player.locale)).replace("{ссылка}", PandorumPlugin.discordServerLink),
                        new String[][] {{Bundle.format("events.hellomsg.ok", findLocale(event.player.locale))}, {Bundle.format("events.hellomsg.disable", findLocale(event.player.locale))}}
                );
            }
        });
        
        bundled(event.player, "events.motd");
    }
}
