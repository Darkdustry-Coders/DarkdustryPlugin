package pandorum.events.listeners;

import arc.util.Log;
import pandorum.discord.Bot;

import java.awt.*;

public class ServerLoadListener {

    public static void call() {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        Bot.sendEmbed(Color.yellow, "Server started!");
    }
}

