package pandorum.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedName;
import static pandorum.Misc.findLocale;
import static pandorum.Misc.sendToChat;
import static pandorum.Misc.nameCheck;

import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.net.Packets.KickReason;

import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.effects.Effects;
import pandorum.models.PlayerInfo;
import org.bson.Document;

public class PlayerJoinEvent {
    public static void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> {
            event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale)));
        });

        if (nameCheck(event.player)) return;

        if (Groups.player.size() >= 1) Vars.state.serverPaused = false;

        sendToChat("events.player-join", colorizedName(event.player));
        Log.info("@ зашёл на сервер, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        Effects.onJoin(event.player);

        Document playerInfo = PandorumPlugin.playersInfo.find((playerInfo2) -> playerInfo2.getString("uuid").equals(event.player.uuid()));
        if (playerInfo == null) {
            playerInfo = PandorumPlugin.playerInfoSchema.create(event.player.uuid(), true, false, "off");
            PandorumPlugin.playersInfo.add(playerInfo);
            PandorumPlugin.savePlayerStats(event.player.uuid());
        }

        if(playerInfo.getBoolean("hellomsg")) {
            String[][] options = {
                {Bundle.format("events.hellomsg.ok", findLocale(event.player.locale))},
                {Bundle.format("events.hellomsg.disable", findLocale(event.player.locale))}
            };
            Call.menu(event.player.con, 1, Bundle.format("events.hellomsg.header", findLocale(event.player.locale)), Bundle.format("events.hellomsg", findLocale(event.player.locale)), options);
        }
        
        bundled(event.player, "events.motd");
        if (PandorumPlugin.config.type == PluginType.anarchy) event.player.admin(true);
        WebhookEmbedBuilder banEmbedBuilder = new WebhookEmbedBuilder()
                .setColor(0x00FF00)
                .setTitle(new WebhookEmbed.EmbedTitle(String.format("%s зашёл на сервер!", Strings.stripColors(event.player.name())), null));
        DiscordWebhookManager.client.send(banEmbedBuilder.build());
    }
}
