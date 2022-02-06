package pandorum.comp;

import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.loginAbuseKickDuration;
import static pandorum.util.Search.findLocale;
import static pandorum.PluginVars.loginWaiting;
import static pandorum.discord.Bot.adminChannel;
import static pandorum.discord.Bot.text;
import static pandorum.util.Utils.*;

public class Authme {

    public static final Button confirm = Button.success("admin.confirm", "Подтвердить");
    public static final Button deny = Button.secondary("admin.deny", "Отклонить");
    public static final Button ban = Button.danger("admin.ban", "Забанить");
    public static final Button check = Button.primary("admin.check", "Информация");

    public static void sendConfirmation(Player player) {
        adminChannel.sendMessage(new MessageBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setColor(Color.cyan)
                        .setTitle("Запрос на выдачу прав администратора.")
                        .addField("Никнейм:", Strings.stripColors(player.name), true)
                        .addField("UUID:", player.uuid(), true)
                        .setFooter("Нажмите на кнопку чтобы подтвердить или отклонить получение прав администратора. Используйте кнопку бана только в крайнем случае.", null)
                        .build()
                ).setActionRows(ActionRow.of(confirm, deny, ban), ActionRow.of(check)).build()).queue(message -> loginWaiting.put(message, player));
    }

    public static void confirm(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "Запрос игрока **@** был подтвержден **@**", Strings.stripColors(player.name), member.getNickname());

            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);
            bundled(player, "commands.login.confirm");
            message.delete().queue();
        }
    }

    public static void deny(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "Запрос игрока **@** был отклонен **@**", Strings.stripColors(player.name), member.getNickname());

            bundled(player, "commands.login.deny");
            message.delete().queue();
        }
    }

    public static void ban(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "**@** заблокировал игрока @ на @ минут", member.getNickname(), Strings.stripColors(player.name), millisecondsToMinutes(loginAbuseKickDuration));

            player.kick(Bundle.format("commands.login.ban", findLocale(player.locale), millisecondsToMinutes(loginAbuseKickDuration)), loginAbuseKickDuration);
            message.delete().queue();
        }
    }

    public static void check(Message message, ButtonClickEvent event) {
        Player player = loginWaiting.get(message);
        if (player != null) {
            PlayerInfo info = player.getInfo();
            event.reply(Strings.format("> Информация об игроке **@**\n\nUUID: @\nIP: @\n\nВошел на сервер: @ раз.\nБыл выгнан с сервера: @ раз\n\nВсе IP адреса: @\n\nВсе никнеймы: @", info.lastName, info.id, info.lastIP, info.timesJoined, info.timesKicked, info.ips, info.names)).setEphemeral(true).queue();
        }
    }
}
