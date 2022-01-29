package pandorum.commands.discord;

import arc.util.Strings;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

import static mindustry.Vars.state;
import static pandorum.PluginVars.mapPlayTime;
import static pandorum.PluginVars.serverUpTime;
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
                .addField("Игроков:", String.valueOf(Groups.player.size()), true)
                .addField("Карта:", Strings.stripColors(state.map.name()), true)
                .addField("Волна:", String.valueOf(state.wave), true)
                .addField("Следующая волна через:", formatDuration((int) state.wavetime / 60 * 1000L), true)
                .addField("Сервер онлайн уже:", formatDuration(serverUpTime * 1000L), true)
                .addField("Времы игры на текущей карте:", formatDuration(mapPlayTime * 1000L), true)
                .build()).queue();
    }
}
