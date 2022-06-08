package pandorum.features;

import arc.util.Strings;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.loginAbuseKickDuration;
import static pandorum.PluginVars.loginWaiting;
import static pandorum.discord.Bot.sendEmbed;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.kick;

public class Authme {

    public static final Button confirm = Button.success("admin.confirm", "Подтвердить"),
            deny = Button.secondary("admin.deny", "Отклонить"),
            ban = Button.danger("admin.ban", "Забанить"),
            info = Button.primary("admin.info", "Информация");

    public static void confirm(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            sendEmbed(message.getChannel(), Color.green, "**@** подтвердил запрос игрока **@**", member.getEffectiveName(), Strings.stripColors(player.name));

            netServer.admins.adminPlayer(player.uuid(), player.usid());
            Ranks.setRank(player.uuid(), Ranks.admin);
            player.admin(true);
            bundled(player, "commands.login.confirm");
            message.delete().queue();
        }
    }

    public static void deny(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            sendEmbed(message.getChannel(), Color.red, "**@** отклонил запрос игрока **@**", member.getEffectiveName(), Strings.stripColors(player.name));

            bundled(player, "commands.login.deny");
            message.delete().queue();
        }
    }

    public static void ban(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            sendEmbed(message.getChannel(), Color.red, "**@** забанил игрока **@** на **@** минут", member.getEffectiveName(), Strings.stripColors(player.name), loginAbuseKickDuration / 1000);

            kick(player, loginAbuseKickDuration, false, "commands.login.ban");
            message.delete().queue();
        }
    }

    public static void info(Message message, ButtonInteractionEvent event) {
        Player player = loginWaiting.get(message);
        if (player != null) {
            PlayerInfo info = player.getInfo();
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
}
