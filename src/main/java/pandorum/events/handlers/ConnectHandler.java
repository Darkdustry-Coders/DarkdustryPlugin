package pandorum.events.handlers;

import arc.Events;
import mindustry.game.EventType.ConnectionEvent;
import mindustry.net.NetConnection;
import mindustry.net.Packets.Connect;

public class ConnectHandler {
    public static void handle(NetConnection con, Connect packet) {
        Events.fire(new ConnectionEvent(con));
    }
}
