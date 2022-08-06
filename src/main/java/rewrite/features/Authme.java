package rewrite.features;

import arc.func.Func;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;

public class Authme {

    public static final Button
            confirm = Button.success("authme.confirm", "Подтвердить"),
            deny = Button.danger("authme.deny", "Отклонить"),
            info = Button.primary("authme.info", "Информация");

    public static void confirm(ButtonInteractionEvent event) {
        remove(event, player -> {
            netServer.admins.adminPlayer(player.uuid(), player.usid());
            Ranks.setRankNet(player.uuid(), Ranks.admin);
            player.admin(true);

            bundled(player, "commands.login.confirm");
            return new EmbedBuilder().setColor(Color.green).setTitle("Запрос подтвержден");
        });
    }

    public static void deny(ButtonInteractionEvent event) {
        remove(event, player -> {
            bundled(player, "commands.login.deny");
            return new EmbedBuilder().setColor(Color.red).setTitle("Запрос отклонён");
        });
    }

    public static void info(ButtonInteractionEvent event) {
        String uuid = loginWaiting.get(event.getMessage());
        PlayerInfo info = netServer.admins.getInfo(uuid);

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.yellow)
                .setTitle("Информация об игроке")
                .addField("Никнейм:", info.lastName, true)
                .addField("UUID:", info.id, true)
                .addField("IP адрес:", info.lastIP, true)
                .addField("Зашел на сервер:", info.timesJoined + " раз", true)
                .addField("Выгнан с сервера:", info.timesKicked + " раз", true)
                .addField("Все никнеймы:", info.names.toString(), true)
                .addField("Все IP адреса", info.ips.toString(), true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private static void remove(ButtonInteractionEvent event, Func<Player, EmbedBuilder> func) {
        String uuid = loginWaiting.remove(event.getMessage());
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) event.getChannel().sendMessageEmbeds(func.get(player)
                .addField("Администратор:", event.getUser().getAsMention(), true)
                .addField("Игрок:", player.name, true).build()).queue();

        event.getMessage().delete().queue();
    }
}
