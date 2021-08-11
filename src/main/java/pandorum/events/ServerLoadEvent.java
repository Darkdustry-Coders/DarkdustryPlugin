package pandorum.events;

import mindustry.game.EventType;
import pandorum.comp.*;

import java.awt.Color;

public class ServerLoadEvent {
    public static void call(final EventType.ServerLoadEvent event) {
        DiscordSender.send("Сервер", "Сервер запущен!", new Color(191, 255, 0));
    }
}
