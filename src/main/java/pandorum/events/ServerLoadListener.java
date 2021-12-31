package pandorum.events;

import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {

    public static void call(final EventType.ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");

        BotHandler.sendEmbed(EmbedCreateSpec.builder().color(BotMain.normalColor).title("Сервер запущен!").build());
    }
}
