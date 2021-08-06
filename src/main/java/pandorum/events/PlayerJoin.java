package pandorum.events;

import arc.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.game.*;

import pandorum.comp.*;
import static pandorum.Misc.*;
import static pandorum.PandorumPlugin.*;

public class PlayerJoin {

    public static void call(final EventType.PlayerJoin event) {
            forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale))));
            if(config.bannedNames.contains(event.player.name())) event.player.con.kick(Bundle.get("events.unofficial-mindustry", findLocale(event.player.locale)), 60000);

            sendToChat("server.player-join", colorizedName(event.player));
            Log.info(event.player.name + " зашёл на сервер, IP: " + event.player.ip() + ", ID: " + event.player.uuid());

            try { joinEffect.spawn(event.player.x, event.player.y); }
            catch (NullPointerException e) {}

            if(config.type == PluginType.anarchy || event.player.uuid().equals("GYmJmGDY2McAAAAAN8z4Bg==")) event.player.admin = true; //TODO добавить uuid главных админов
            Call.infoMessage(event.player.con, Bundle.format("server.hellomsg", findLocale(event.player.locale)));
            bundled(event.player, "server.motd");
    }
}
