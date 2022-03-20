package pandorum.events.handlers;

import arc.Events;
import arc.func.Cons2;
import mindustry.game.EventType.ConnectionEvent;
import mindustry.net.NetConnection;
import mindustry.net.Packets.Connect;

public class ConnectHandler implements Cons2<NetConnection, Connect> {

    public void get(NetConnection con, Connect packet) {
        Events.fire(new ConnectionEvent(con));
    }
}
