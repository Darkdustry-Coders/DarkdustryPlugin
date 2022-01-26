package pandorum.commands.discord;

import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlaytime;
import static pandorum.PluginVars.serverUptime;
import static pandorum.discord.Bot.err;
import static pandorum.util.Utils.formatDuration;

public class StatusCommand {
    public static void run(final String[] args, final Message message) {
        if (state.isMenu()) {
            err(message.getChannel(), ":x: Сервер отключен.", "Попросите администраторов запустить его.");
            return;
        }

        message.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle(":desktop: Статус сервера:")
                .addField("Игроков:", String.valueOf(Groups.player.size()), false)
                .addField("Карта:", state.map.name(), false)
                .addField("Волна:", String.valueOf(state.wave), false)
                .addField("Сервер онлайн уже:", formatDuration(serverUptime), false)
                .addField("Времы игры на текущей карте:", formatDuration(mapPlaytime), false)
                .build()).queue();
    }
}
