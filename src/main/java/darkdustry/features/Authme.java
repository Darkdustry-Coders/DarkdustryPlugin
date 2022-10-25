package darkdustry.features;

import arc.func.Func2;
import darkdustry.utils.Find;
import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.discord.Bot.*;
import static darkdustry.discord.Bot.Palette.*;
import static mindustry.Vars.netServer;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromFormatted;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;

public class Authme {

    public static final SelectMenu menu = SelectMenu.create("authme")
            .addOption("Confirm", "authme.confirm", "Confirm request.", fromFormatted("✅"))
            .addOption("Deny", "authme.deny", "Deny request.", fromFormatted("❌"))
            .addOption("Information", "authme.info", "Look up all information about the player.", fromFormatted("🔎"))
            .setPlaceholder("Select an action...").build();

    public static void sendAdminRequest(Player player) {
        if (adminChannel == null || !adminChannel.canTalk()) return;

        adminChannel.sendMessageEmbeds(new EmbedBuilder()
                .setColor(info)
                .setTitle("Запрос на получение прав администратора.")
                .addField("Nickname:", player.plainName(), true)
                .addField("UUID:", player.uuid(), true)
                .setFooter("Выберите нужную опцию, чтобы подтвердить или отклонить запрос. Подтверждайте только свои запросы!")
                .build()
        ).setComponents(of(menu)).queue(message -> loginWaiting.put(message, player.getInfo()));
    }

    public static void confirm(SelectMenuInteractionEvent event) {
        remove(event, (info, player) -> {
            netServer.admins.adminPlayer(info.id, info.adminUsid);

            if (player != null) {
                player.admin(true);
                bundled(player, "commands.login.confirm");
            }

            return new EmbedBuilder().setColor(success).setTitle("Request Confirmed");
        });
    }

    public static void deny(SelectMenuInteractionEvent event) {
        remove(event, (info, player) -> {
            if (player != null) bundled(player, "commands.login.deny");
            return new EmbedBuilder().setColor(error).setTitle("Request Denied");
        });
    }

    public static void information(SelectMenuInteractionEvent event) {
        var playerInfo = loginWaiting.get(event.getMessage());

        var embed = new EmbedBuilder().setColor(info)
                .setTitle(":mag: Player Info")
                .addField("Nickname:", playerInfo.plainLastName(), true)
                .addField("UUID:", playerInfo.id, true)
                .addField("IP:", playerInfo.lastIP, true)
                .addField("Times joined:", String.valueOf(playerInfo.timesJoined), true)
                .addField("Times kicked:", String.valueOf(playerInfo.timesKicked), true)
                .addField("All nicknames:", playerInfo.names.toString(), true)
                .addField("All IPs", playerInfo.ips.toString(), true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private static void remove(SelectMenuInteractionEvent event, Func2<PlayerInfo, Player, EmbedBuilder> func) {
        var info = loginWaiting.remove(event.getMessage());
        var player = Find.playerByUuid(info.id);

        event.getChannel().sendMessageEmbeds(func.get(info, player)
                .addField("Administrator:", event.getUser().getAsMention(), true)
                .addField("Player:", info.plainLastName(), true).build()).queue();

        event.getMessage().delete().queue();
    }
}