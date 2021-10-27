package pandorum.events;

import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {
    public static void call(final EventType.ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер запущен и готов к работе!");

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.normalColor)
                .setTitle("Сервер запущен.");

        Time.runTask(1f, () -> {
            try {
                BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
            } catch (NullPointerException ignored) {}
        });
    }
}
