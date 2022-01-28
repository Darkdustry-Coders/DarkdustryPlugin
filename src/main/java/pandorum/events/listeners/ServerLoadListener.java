package pandorum.events.listeners;

import arc.util.Log;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

import static pandorum.discord.Bot.botChannel;

public class ServerLoadListener {

    public static void call() {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        botChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("Сервер запущен!").setColor(Color.green).build()).queue();
    }
}

