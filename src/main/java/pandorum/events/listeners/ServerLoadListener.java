package pandorum.events.listeners;

import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType.ServerLoadEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

import static pandorum.discord.Bot.botChannel;
import static pandorum.PluginVars.serverUptime;

public class ServerLoadListener {

    public static void call(final ServerLoadEvent event) {
        Log.info("[Darkdustry]: Сервер готов к работе...");
        botChannel.sendMessageEmbeds(new EmbedBuilder().setTitle("Сервер запущен!").setColor(Color.green).build()).queue();

        serverUptime = Time.millis();
    }
}

