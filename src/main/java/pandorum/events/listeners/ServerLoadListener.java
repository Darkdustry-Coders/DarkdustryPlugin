package pandorum.events.listeners;

import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType.ServerLoadEvent;

import static pandorum.discord.BotHandler.normalColor;
import static pandorum.discord.BotHandler.sendEmbed;

public class ServerLoadListener {

    public static void call(final ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        sendEmbed(EmbedCreateSpec.builder().color(normalColor).title("Сервер запущен!").build());
    }
}
