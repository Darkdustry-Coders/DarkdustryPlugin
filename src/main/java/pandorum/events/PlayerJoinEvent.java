package pandorum.events;

import static pandorum.Misc.*;
import static pandorum.effects.Effects.onJoin;

import pandorum.comp.Config.PluginType;
import pandorum.comp.*;
import pandorum.PandorumPlugin;

import arc.util.Log;
import mindustry.game.*;
import mindustry.gen.*;

public class PlayerJoinEvent {
    public void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale))));
        if(Config.bannedNames.contains(event.player.name())) event.player.con.kick(Bundle.get("events.unofficial-mindustry", findLocale(event.player.locale)), 60000);

        sendToChat("server.player-join", colorizedName(event.player));
        Log.info(event.player.name + " зашёл на сервер, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

        onJoin(event.player);

        if(Config.type == PluginType.anarchy || event.player.uuid().equals("GYmJmGDY2McAAAAAN8z4Bg==")) event.player.admin = true;
        Call.infoMessage(event.player.con, Bundle.format("server.hellomsg", findLocale(event.player.locale)));
        bundled(event.player, "server.motd");
    }
}
