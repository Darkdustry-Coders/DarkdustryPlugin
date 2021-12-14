package pandorum.events;

import arc.util.Log;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {

    public static void call(final EventType.ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(BotMain.normalColor)
                .title("Сервер запущен!")
                .build();

        BotHandler.sendEmbed(embed);
    }
}
