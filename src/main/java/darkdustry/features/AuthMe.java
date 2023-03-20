package darkdustry.features;

import darkdustry.utils.Find;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.gen.Player;

import java.util.Collections;

import static darkdustry.discord.Bot.adminChannel;
import static mindustry.Vars.netServer;
import static useful.Bundle.bundled;

public class AuthMe {

    public static void sendAdminRequest(Player player) {
        if (adminChannel == null) return;

        adminChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.VIVID_VIOLET)
                .title("Request for administrator rights.")
                .addField("Nickname:", player.plainName(), true)
                .addField("UUID:", player.uuid(), true)
                .footer("Select the desired option to confirm or deny the request. Confirm only your requests!", null)
                .build()
        ).withComponents(ActionRow.of(SelectMenu.of(
                player.uuid(),
                Option.of("Confirm", "confirm").withDescription("Confirm request.").withEmoji(ReactionEmoji.unicode("✅")),
                Option.of("Deny", "deny").withDescription("Deny request.").withEmoji(ReactionEmoji.unicode("❌")),
                Option.of("Info", "info").withDescription("Look up all information about the player.").withEmoji(ReactionEmoji.unicode("🔎"))
        ))).subscribe();
    }

    public static void confirm(SelectMenuInteractionEvent event) {
        var info = netServer.admins.getInfoOptional(event.getCustomId());
        if (info == null) return;

        netServer.admins.adminPlayer(info.id, info.adminUsid);

        var player = Find.playerByUuid(info.id);
        if (player != null) {
            player.admin(true);
            bundled(player, "commands.login.success");
        }

        event.edit().withEmbeds(EmbedCreateSpec.builder()
                .color(Color.MEDIUM_SEA_GREEN)
                .title("Request Confirmed")
                .addField("Administrator:", event.getInteraction().getUser().getMention(), true)
                .addField("Player:", info.plainLastName(), true)
                .build()).withComponents(Collections.emptyList()).subscribe();
    }

    public static void deny(SelectMenuInteractionEvent event) {
        var info = netServer.admins.getInfoOptional(event.getCustomId());
        if (info == null) return;

        var player = Find.playerByUuid(info.id);
        if (player != null)
            bundled(player, "commands.login.fail");

        event.edit().withEmbeds(EmbedCreateSpec.builder()
                .color(Color.CINNABAR)
                .title("Request Denied")
                .addField("Administrator:", event.getInteraction().getUser().getMention(), true)
                .addField("Player:", info.plainLastName(), true)
                .build()).withComponents(Collections.emptyList()).subscribe();
    }

    public static void info(SelectMenuInteractionEvent event) {
        var info = netServer.admins.getInfoOptional(event.getCustomId());
        if (info == null) return;

        event.reply().withEmbeds(EmbedCreateSpec.builder()
                .color(Color.SUMMER_SKY)
                .title("Player Info")
                .addField("UUID:", info.id, false)
                .addField("IP:", info.lastIP, false)
                .addField("Times joined:", String.valueOf(info.timesJoined), false)
                .addField("Times kicked:", String.valueOf(info.timesKicked), false)
                .addField("All nicknames:", info.names.toString(), false)
                .addField("All IPs", info.ips.toString(), false)
                .build()).withEphemeral(false).subscribe();
    }
}