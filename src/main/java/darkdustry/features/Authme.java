package darkdustry.features;

import arc.func.Func;
import mindustry.gen.*;
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
        remove(event, player -> {
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);

            bundled(player, "commands.login.confirm");
            return success("Запрос подтвержден");
        });
    }

    public static void deny(GenericComponentInteractionCreateEvent event) {
        remove(event, player -> {
            bundled(player, "commands.login.deny");
            return error("Запрос отклонен");
        });
    }

    public static void information(GenericComponentInteractionCreateEvent event) {
        String uuid = loginWaiting.get(event.getMessage());
        var info = netServer.admins.getInfo(uuid);

        EmbedBuilder embed = info("Информация об игроке")
                .addField("Никнейм:", info.lastName, true)
                .addField("UUID:", info.id, true)
                .addField("IP адрес:", info.lastIP, true)
                .addField("Зашел на сервер:", info.timesJoined + " раз", true)
                .addField("Выгнан с сервера:", info.timesKicked + " раз", true)
                .addField("Все никнеймы:", info.names.toString(), true)
                .addField("Все IP адреса", info.ips.toString(), true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private static void remove(GenericComponentInteractionCreateEvent event, Func<Player, EmbedBuilder> func) {
        String uuid = loginWaiting.remove(event.getMessage());
        var player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) event.getChannel().sendMessageEmbeds(func.get(player)
                .addField("Администратор:", event.getUser().getAsMention(), true)
                .addField("Игрок:", player.name, true).build()).queue();

        event.getMessage().delete().queue();
    }
}
