package pandorum.events.listeners;

import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType.ServerLoadEvent;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {

    public static void call(final ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.normalColor).title("Сервер запущен!").build());
    }
}
