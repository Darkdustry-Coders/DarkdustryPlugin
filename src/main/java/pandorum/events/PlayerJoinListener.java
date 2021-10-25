package pandorum.events;

import arc.util.Log;
import arc.util.Strings;
import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Call;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;
import pandorum.comp.effects.Effects;
import pandorum.models.PlayerModel;
import pandorum.comp.Ranks;

import static pandorum.Misc.*;

public class PlayerJoinListener {
    public static void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale))));

        if (nameCheck(event.player)) return;

        Ranks.getRank(event.player, rank -> event.player.name(rank.tag + event.player.getInfo().lastName));

        sendToChat("events.player-join", event.player.coloredName());
        Log.info("@ зашёл на сервер, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.successColor)
                .setTitle(Strings.format("**@** зашел на сервер!", Strings.stripColors(event.player.name)));

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();

        Effects.onJoin(event.player);

        PlayerModel.find(new BasicDBObject("UUID", event.player.uuid()), playerInfo -> {
            if (playerInfo.hellomsg) {
                String[][] options = {{Bundle.format("events.hellomsg.ok", findLocale(event.player.locale))}, {Bundle.format("events.hellomsg.disable", findLocale(event.player.locale))}};
                Call.menu(event.player.con, 0, Bundle.format("events.hellomsg.header", findLocale(event.player.locale)), Bundle.format("events.hellomsg", findLocale(event.player.locale)), options);
            }
        });
        
        bundled(event.player, "events.motd");
    }
}
