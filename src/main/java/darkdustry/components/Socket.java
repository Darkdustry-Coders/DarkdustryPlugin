package darkdustry.components;

import arc.func.Cons;
import com.ospx.sock.*;
import com.ospx.sock.EventBus.*;
import darkdustry.DarkdustryPlugin;

import static darkdustry.PluginVars.*;

public class Socket {

    public static Sock sock;

    public static void connect() {
        try {
            sock = Sock.create(config.sockPort, config.mode.isSockServer);
            sock.connect();
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect the socket: @", e);
        }
    }

    public static void send(Object value) {
        sock.send(value);
    }

    public static <T> EventSubscription<T> on(Class<T> type, Cons<T> listener) {
        return sock.on(type, listener);
    }

    public static <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, Runnable expired) {
        return sock.request(request, listener, expired);
    }

    public static <T extends Response> void respond(Request<T> request, T response) {
        sock.respond(request, response);
    }
}