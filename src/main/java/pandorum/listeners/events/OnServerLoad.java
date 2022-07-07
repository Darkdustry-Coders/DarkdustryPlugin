package pandorum.listeners.events;

import arc.func.Cons;
import arc.util.Log;
import mindustry.game.EventType.ServerLoadEvent;
import pandorum.discord.Bot;

import java.awt.*;

import static pandorum.discord.Bot.botChannel;

public class OnServerLoad implements Cons<ServerLoadEvent> {

    public void get(ServerLoadEvent event) {
        Log.info("[Darkdustry] Сервер готов к работе");
        Bot.sendEmbed(botChannel, Color.yellow, "Сервер запущен.");
    }
}

