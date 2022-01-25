package pandorum.commands.discord;

import arc.Core;
import arc.util.Strings;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

import static mindustry.Vars.state;
import static pandorum.discord.Bot.err;

public class StatusCommand {
    public static void run(final String[] args, final Message message) {
        if (state.isMenu()) {
            err(message.getChannel(), "Сервер отключен.", "Попросите администраторов запустить его.");
            return;
        }

        message.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle("Статус сервера:")
                .addField("Игроков:", String.valueOf(Groups.player.size()), false)
                .addField("Карта:", state.map.name(), false)
                .addField("Волна:", String.valueOf(state.wave), false)
                .addField("Потребление ОЗУ:", Strings.format("@ MB", Core.app.getJavaHeap() / 1024 / 1024), false)
                .addField("TPS на сервере:", String.valueOf(Core.graphics.getFramesPerSecond()), false)
                .build()).queue();
    }
}
