package darkdustry.features;

import darkdustry.utils.Find;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.*;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.gen.Player;
import useful.Bundle;

import java.util.Collections;

import static darkdustry.discord.Bot.*;
import static mindustry.Vars.*;

public class AuthMe {

    public static void sendAdminRequest(Player player) {
        if (!connected) return;

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

        var player = Find.playerByUuid(info.id);
        if (player != null) {
            player.admin(true);
            Bundle.send(player, "commands.login.success");
        }

        netServer.admins.adminPlayer(info.id, player == null ? info.adminUsid : player.usid());
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
            Bundle.send(player, "commands.login.fail");
        if (player != null) {
            player.admin(false);
            bundled(player, "commands.login.fail");
        }

        netServer.admins.unAdminPlayer(info.id);
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
                .build()).withEphemeral(true).subscribe();
    }
}