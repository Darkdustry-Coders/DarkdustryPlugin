package pandorum.events;

import arc.util.Strings;
import mindustry.game.EventType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import pandorum.Misc;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

public class AdminRequestListener {
    public static void call(final EventType.AdminRequestEvent event) {
        switch (event.action) {
            case wave -> Misc.sendToChat("events.admin.wave-skip", event.player.coloredName());
            case kick -> {
                Misc.sendToChat("events.admin.kick", event.player.coloredName(), event.other.coloredName());

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.errorColor)
                        .setAuthor("KICK")
                        .setTitle("Игрок был выгнан с сервера!")
                        .addField("Админом: ", Strings.stripColors(event.player.name), false)
                        .addField("Никнейм: ", Strings.stripColors(event.other.name), false)
                        .addField("IP: ", event.other.ip(), false);

                BotHandler.sendEmbed(embed);
            }
        }
    }
}
