package pandorum.events;

import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class PlayerBanListener {
    public static void call(final EventType.PlayerBanEvent event) {
        PlayerInfo info = netServer.admins.getInfo(event.uuid);
        if (info != null) {

        }
    }
}
