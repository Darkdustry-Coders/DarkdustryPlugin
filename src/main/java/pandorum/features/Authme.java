package pandorum.features;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.loginWaiting;
import static pandorum.util.PlayerUtils.bundled;

public class Authme {

    public static final Button confirm = Button.success("authme.confirm", "Подтвердить"),
            deny = Button.danger("authme.deny", "Отклонить"),
            info = Button.primary("authme.info", "Информация");

    public static void confirm(ButtonInteractionEvent event) {
        String uuid = loginWaiting.remove(event.getMessage());
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) {
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.green)
                    .setTitle("Запрос подтвержден")
                    .addField("Администратор:", event.getUser().getAsMention(), true)
                    .addField("Игрок:", player.name, true)
                    .build()).queue();

            netServer.admins.adminPlayer(player.uuid(), player.usid());
            Ranks.setRank(player.uuid(), Ranks.admin);
            player.admin(true);

            bundled(player, "commands.login.confirm");
        }

        event.getMessage().delete().queue();
    }

    public static void deny(ButtonInteractionEvent event) {
        String uuid = loginWaiting.remove(event.getMessage());
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) {
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("Запрос отклонён")
                    .addField("Администратор:", event.getUser().getAsMention(), true)
                    .addField("Игрок:", player.name, true)
                    .build()).queue();

            bundled(player, "commands.login.deny");
        }

        event.getMessage().delete().queue();
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
}

