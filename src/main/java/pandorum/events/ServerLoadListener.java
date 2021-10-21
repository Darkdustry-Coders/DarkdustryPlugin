package pandorum.events;

import arc.util.Strings;
import mindustry.game.EventType;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class ServerLoadListener {
    public static void call(final EventType.ServerLoadEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(BotMain.successColor)
                .setTitle("Сервер запущен!");

        BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
    }
}