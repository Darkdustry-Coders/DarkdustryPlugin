package pandorum.events;

import mindustry.game.EventType;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;

public class PlayerUnbanListener {
    public static void call(final EventType.PlayerUnbanEvent event) {
        PlayerInfo info = netServer.admins.getInfo(event.uuid);
        if (info != null) {

        }
    }
}
