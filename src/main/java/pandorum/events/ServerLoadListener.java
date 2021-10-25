package pandorum.events;

import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {
    public static void call(final EventType.ServerLoadEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(BotMain.successColor)
            .setTitle("Сервер запущен!");

        Time.runTask(7.5f, () -> {
            Log.info("[Darkdustry]: Сервер запущен и готов к работе!");
            // Проверка на нулл, бот мог еще не запуститься
            if (BotHandler.botChannel != null) BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }
}
