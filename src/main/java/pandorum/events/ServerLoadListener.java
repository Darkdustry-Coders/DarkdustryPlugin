package pandorum.events;

import arc.util.Log;
import mindustry.game.EventType;

public class ServerLoadListener {
    public static void call(final EventType.ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер запущен и готов к работе!");
    }
}
