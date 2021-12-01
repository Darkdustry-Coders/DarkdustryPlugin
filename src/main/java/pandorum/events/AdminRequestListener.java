package pandorum.events;

import arc.util.Strings;
import discord4j.core.spec.EmbedCreateSpec;
import mindustry.game.EventType;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static pandorum.Misc.sendToChat;

public class AdminRequestListener {
    public static void call(final EventType.AdminRequestEvent event) {
        switch (event.action) {
            case wave -> sendToChat("events.admin.wave-skip", event.player.coloredName());
            case kick -> {
                sendToChat("events.admin.kick", event.player.coloredName(), event.other.coloredName());

                EmbedCreateSpec embed = EmbedCreateSpec.builder()
                        .color(BotMain.errorColor)
                        .author("KICK", null, "https://thumbs.dreamstime.com/b/red-cross-symbol-icon-as-delete-remove-fail-failure-incorr-incorrect-answer-89999776.jpg")
                        .title("Игрок был выгнан с сервера!")
                        .addField("Никнейм игрока:", Strings.stripColors(event.other.name), false)
                        .addField("UUID игрока:", event.other.uuid(), false)
                        .addField("Никнейм админа:", Strings.stripColors(event.player.name), false)
                        .build();

                BotHandler.sendEmbed(embed);
            }
            case ban -> {
                sendToChat("events.admin.ban", event.player.coloredName(), event.other.coloredName());

                EmbedCreateSpec embed = EmbedCreateSpec.builder()
                        .color(BotMain.errorColor)
                        .author("BAN", null, "https://www.iconsdb.com/icons/preview/red/ban-xxl.png")
                        .title("Игрок был заблокирован на сервере!")
                        .addField("Никнейм игрока:", Strings.stripColors(event.other.name), false)
                        .addField("UUID игрока:", event.other.uuid(), false)
                        .addField("Никнейм админа:", Strings.stripColors(event.player.name), false)
                        .build();

                BotHandler.sendEmbed(embed);
            }
        }
    }
}
