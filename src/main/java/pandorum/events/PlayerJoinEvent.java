package pandorum.events;

import static pandorum.Misc.bundled;
import static pandorum.Misc.colorizedName;
import static pandorum.Misc.findLocale;
import static pandorum.Misc.sendToChat;

import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Call;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;
import pandorum.comp.Config.PluginType;
import pandorum.comp.DiscordWebhookManager;
import pandorum.effects.Effects;

public class PlayerJoinEvent {
    public static void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> {
            event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale)));
        });

        sendToChat("server.player-join", colorizedName(event.player));
        Log.info(event.player.name + " зашёл на сервер, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        Effects.onJoin(event.player);

        if (PandorumPlugin.config.type == PluginType.anarchy) event.player.admin(true);
        Call.infoMessage(event.player.con, Bundle.format("server.hellomsg", findLocale(event.player.locale)));
        bundled(event.player, "server.motd");

        DiscordWebhookManager.client.send(String.format("**%s зашел(ла/ло) на сервер!**", event.player.name()));
    }
}