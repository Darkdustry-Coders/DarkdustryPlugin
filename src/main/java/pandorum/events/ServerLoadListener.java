package pandorum.events;

import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import pandorum.annotations.events.EventListener;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {
    //@EventListener(eventType = EventType.ServerLoadEvent.class)
    public static void call(final EventType.ServerLoadEvent event) {
        Log.info("[Darkdustry]: Плагин загружен. Сервер готов к работе...");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.normalColor)
                .title("Сервер запущен!")
                .build();

        BotHandler.sendEmbed(embed);
    }
}
