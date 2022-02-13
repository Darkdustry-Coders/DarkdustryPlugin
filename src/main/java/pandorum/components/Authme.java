package pandorum.components;

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
import static pandorum.PluginVars.loginWaiting;
import static pandorum.discord.Bot.adminChannel;
import static pandorum.discord.Bot.text;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.bundled;
import static pandorum.util.Utils.millisecondsToMinutes;

public class Authme {

    public static final Button confirm = Button.success("admin.confirm", "Confirm");
    public static final Button deny = Button.secondary("admin.deny", "Deny");
    public static final Button ban = Button.danger("admin.ban", "Ban");
    public static final Button check = Button.primary("admin.check", "Information");

    public static void sendConfirmation(Player player) {
        adminChannel.sendMessage(new MessageBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setColor(Color.cyan)
                        .setTitle("Request for administrator rights.")
                        .addField("Nickname:", Strings.stripColors(player.name), true)
                        .addField("UUID:", player.uuid(), true)
                        .setFooter("Click the button to confirm or deny the request.", null)
                        .build()
                ).setActionRows(ActionRow.of(confirm, deny, ban, check)).build()).queue(message -> loginWaiting.put(message, player));
    }

    public static void confirm(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "A request of player **@** was confirmed by **@**", Strings.stripColors(player.name), member.getEffectiveName());

            netServer.admins.adminPlayer(player.uuid(), player.usid());
            player.admin(true);
            bundled(player, "commands.login.confirm");
            message.delete().queue();
        }
    }

    public static void deny(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "A request of player **@** was denied by **@**", Strings.stripColors(player.name), member.getEffectiveName());

            bundled(player, "commands.login.deny");
            message.delete().queue();
        }
    }

    public static void ban(Message message, Member member) {
        Player player = loginWaiting.remove(message);
        if (player != null) {
            text(message.getChannel(), "**@** banned a player **@** for **@** minutes", member.getEffectiveName(), Strings.stripColors(player.name), millisecondsToMinutes(loginAbuseKickDuration));

            player.kick(Bundle.format("commands.login.ban", findLocale(player.locale), millisecondsToMinutes(loginAbuseKickDuration)), loginAbuseKickDuration);
            message.delete().queue();
        }
    }

    public static void check(Message message, ButtonClickEvent event) {
        Player player = loginWaiting.get(message);
        if (player != null) {
            PlayerInfo info = player.getInfo();
            event.reply(Strings.format("> Information about player **@**\n\nUUID: @\nIP: @\n\nTimes joined: @ раз.\nTimes kicked: @ раз\n\nAll IPs: @\n\nAll nicknames: @", info.lastName, info.id, info.lastIP, info.timesJoined, info.timesKicked, info.ips, info.names)).setEphemeral(true).queue();
        }
    }
}
