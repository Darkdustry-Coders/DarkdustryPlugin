package pandorum.events.listeners;

import arc.util.Log;
import pandorum.discord.Bot;

import java.awt.*;

public class ServerLoadListener implements Runnable {

    public void run() {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        Bot.sendEmbed(Color.yellow, "Сервер запущен...");
    }
}

