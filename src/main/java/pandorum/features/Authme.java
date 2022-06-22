package pandorum.features;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import pandorum.discord.Context;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.loginWaiting;
import static pandorum.util.PlayerUtils.bundled;

public class Authme {

    public static final Button confirm = Button.success("admin.confirm", "Подтвердить"),
            deny = Button.danger("admin.deny", "Отклонить"),
            info = Button.primary("admin.info", "Информация");

    public static void confirm(Context context) {
        String uuid = loginWaiting.remove(context.message);
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) {
            context.success("Запрос подтвержден.", "**@** подтвердил запрос игрока **@**.", context.member.getEffectiveName(), player.name);

            netServer.admins.adminPlayer(player.uuid(), player.usid());
            Ranks.setRank(player.uuid(), Ranks.admin);
            player.admin(true);

            bundled(player, "commands.login.confirm");
        }

        context.message.delete().queue();
    }

    public static void deny(Context context) {
        String uuid = loginWaiting.remove(context.message);
        Player player = Groups.player.find(p -> p.uuid().equals(uuid));

        if (player != null) {
            context.err("Запрос отклонён.", "**@** отклонил запрос игрока **@**.", context.member.getEffectiveName(), player.name);
            bundled(player, "commands.login.deny");
        }

        context.message.delete().queue();
    }

    public static void info(Context context, ButtonInteractionEvent event) {
        String uuid = loginWaiting.get(context.message);
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

