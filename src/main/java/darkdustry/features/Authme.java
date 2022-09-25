package darkdustry.features;

import arc.func.*;
import darkdustry.utils.Find;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.discord.Bot.*;
import static mindustry.Vars.netServer;

public class Authme {

    public static final SelectMenu menu = SelectMenu.create("authme")
            .addOption("Подтвердить", "authme.confirm", "Подтвердить запрос.", Emoji.fromFormatted("✅"))
            .addOption("Отклонить", "authme.deny", "Отклонить запрос.", Emoji.fromFormatted("❌"))
            .addOption("Информация", "authme.info", "Посмотреть всю информацию об игроке.", Emoji.fromFormatted("🔎"))
            .setPlaceholder("Выбери действие...").build();

    public static void confirm(GenericComponentInteractionCreateEvent event) {
        remove(event, (info, player) -> {
            netServer.admins.adminPlayer(info.id, info.adminUsid);

            if (player != null) {
                player.admin(true);
                bundled(player, "commands.login.confirm");
            }

            return success("Запрос подтвержден");
        });
    }

    public static void deny(GenericComponentInteractionCreateEvent event) {
        remove(event, (info, player) -> {
            if (player != null) bundled(player, "commands.login.deny");
            return error("Запрос отклонен");
        });
    }

    public static void information(GenericComponentInteractionCreateEvent event) {
        var info = loginWaiting.get(event.getMessage());

        var embed = info("Информация об игроке")
                .addField("Никнейм:", info.plainLastName(), true)
                .addField("UUID:", info.id, true)
                .addField("IP адрес:", info.lastIP, true)
                .addField("Зашел на сервер:", info.timesJoined + " раз", true)
                .addField("Выгнан с сервера:", info.timesKicked + " раз", true)
                .addField("Все никнеймы:", info.names.toString(), true)
                .addField("Все IP адреса", info.ips.toString(), true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private static void remove(GenericComponentInteractionCreateEvent event, Func2<PlayerInfo, Player, EmbedBuilder> func) {
        var info = loginWaiting.remove(event.getMessage());
        var player = Find.playerByUuid(info.id);

        event.getChannel().sendMessageEmbeds(func.get(info, player)
                .addField("Администратор:", event.getUser().getAsMention(), true)
                .addField("Игрок:", info.plainLastName(), true).build()).queue();

        event.getMessage().delete().queue();
    }
}
