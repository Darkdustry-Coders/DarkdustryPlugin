package pandorum.events;

import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Call;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Bundle;
import pandorum.effects.Effects;
import pandorum.ranks.Ranks;

import static pandorum.Misc.*;

public class PlayerJoinListener {
    public static void call(final EventType.PlayerJoin event) {
        PandorumPlugin.forbiddenIps.each(i -> i.matchIp(event.player.con.address), i -> event.player.con.kick(Bundle.get("events.vpn-ip", findLocale(event.player.locale))));

        if (nameCheck(event.player)) return;

        event.player.name(Ranks.getRank(event.player).tag + event.player.coloredName());

        sendToChat("events.player-join", event.player.coloredName());
        Log.info("@ зашёл на сервер, IP: @, ID: @", event.player.name, event.player.ip(), event.player.uuid());

        Effects.onJoin(event.player);

        Document playerInfo = PandorumPlugin.createInfo(event.player);

        if (playerInfo.getBoolean("hellomsg")) {
            String[][] options = {{Bundle.format("events.hellomsg.ok", findLocale(event.player.locale))}, {Bundle.format("events.hellomsg.disable", findLocale(event.player.locale))}};
            Call.menu(event.player.con, 0, Bundle.format("events.hellomsg.header", findLocale(event.player.locale)), Bundle.format("events.hellomsg", findLocale(event.player.locale)), options);
        }
        
        bundled(event.player, "events.motd");
    }
}
