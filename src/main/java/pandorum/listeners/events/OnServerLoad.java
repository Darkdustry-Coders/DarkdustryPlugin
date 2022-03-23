package pandorum.listeners.events;

import arc.func.Cons;
import arc.util.Log;
import mindustry.game.EventType.ServerLoadEvent;
import pandorum.discord.Bot;

import java.awt.*;

public class OnServerLoad implements Cons<ServerLoadEvent> {

    public void get(ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        Bot.sendEmbed(Color.yellow, "Сервер запущен...");
    }
}

