package pandorum.commands.discord;

import arc.Core;
import arc.util.Strings;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.gen.Groups;

import static mindustry.Vars.state;
import static pandorum.discord.Bot.*;

public class StatusCommand {
    public static void run(final String[] args, final Message message) {
        if (state.isMenu()) {
            err(message.getChannel().block(), "Сервер отключен.", "Попросите администраторов запустить его.");
            return;
        }

        sendEmbed(message.getChannel().block(), EmbedCreateSpec.builder()
                .color(successColor)
                .author("Статус сервера", null, "https://icon-library.com/images/yes-icon/yes-icon-15.jpg")
                .title("Сервер онлайн.")
                .addField("Игроков:", String.valueOf(Groups.player.size()), false)
                .addField("Карта:", state.map.name(), false)
                .addField("Волна:", String.valueOf(state.wave), false)
                .addField("Потребление ОЗУ:", Strings.format("@ MB", Core.app.getJavaHeap() / 1024 / 1024), false)
                .addField("TPS на сервере:", String.valueOf(Core.graphics.getFramesPerSecond()), false)
                .footer("Используй " + discordHandler.getPrefix() + "players, чтобы посмотреть список всех игроков.", null)
                .build()
        );
    }
}
