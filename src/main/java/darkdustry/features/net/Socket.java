package darkdustry.features.net;

import arc.func.Cons;
import arc.util.Log;
import com.ospx.sock.EventBus.*;
import com.ospx.sock.Sock;

import static darkdustry.config.Config.*;

public class Socket {

    /** Сокет, через который сервер обменивается событиями. */
    public static Sock socket;

    public static void connect() {
        try {
            socket = Sock.create(config.sockPort, config.mode.isMainServer);
            socket.connect();
        } catch (Exception e) {
            Log.err("Failed to connect socket", e);
        }
    }

    public static boolean isConnected() {
        return socket.isConnected();
    }

    public static void send(Object value) {
        socket.send(value);
    }

    public static <T> void on(Class<T> type, Cons<T> listener) {
        socket.on(type, listener);
    }

    public static <T extends Response> void request(Request<T> request, Cons<T> listener) {
        socket.request(request, listener).withTimeout(3f);
    }

    public static <T extends Response> void request(Request<T> request, Cons<T> listener, Runnable expired) {
        socket.request(request, listener, expired).withTimeout(3f);
    }

    public static <T extends Response> void respond(Request<T> request, T response) {
        socket.respond(request, response);
    }
}