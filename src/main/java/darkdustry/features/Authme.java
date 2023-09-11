package darkdustry.features;

import darkdustry.components.*;
import darkdustry.components.Database.*;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.Find;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.*;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import useful.Bundle;

import java.util.Collections;

import static arc.util.Strings.*;
import static darkdustry.discord.DiscordBot.*;
import static discord4j.common.util.TimestampFormat.*;
import static discord4j.rest.util.Color.*;
import static mindustry.Vars.*;

public class Authme {

    public static void sendBan(String server, Ban ban) {
        if (!connected) return;

        banChannel.createMessage(EmbedCreateSpec.builder()
                .color(CINNABAR)
                .title("Ban")
                .addField("Server:", capitalize(server), false)
                .addField("ID:", String.valueOf(ban.id), false)
                .addField("Player:", ban.player, false)
                .addField("Admin:", ban.admin, false)
                .addField("Reason:", ban.reason, false)
                .addField("Unban Date:", LONG_DATE.format(ban.unbanDate.toInstant()), false)
                .build()).subscribe();
    }

    public static void sendAdminRequest(String server, PlayerData data) {
        if (!connected) return;

        adminChannel.createMessage(EmbedCreateSpec.builder()
                .color(BISMARK)
                .title("Admin Request")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .addField("Server:", capitalize(server), false)
                .footer("Select the desired option to confirm or deny the request. Confirm only your requests!", null)
                .build()
        ).withComponents(ActionRow.of(SelectMenu.of("admin-request",
                Option.of("Confirm", "confirm-" + server + "-" + data.uuid).withDescription("Confirm this request.").withEmoji(ReactionEmoji.unicode("✅")),
                Option.of("Deny", "deny-" + server + "-" + data.uuid).withDescription("Deny this request.").withEmoji(ReactionEmoji.unicode("❌"))
        ))).subscribe();
    }

    public static void confirm(SelectMenuInteractionEvent event, String server, String uuid) {
        Socket.send(new AdminRequestConfirmEvent(server, uuid));

        var data = Database.getPlayerData(uuid);
        if (data == null) return; // Just in case

        event.edit().withEmbeds(EmbedCreateSpec.builder()
                .color(MEDIUM_SEA_GREEN)
                .title("Admin Request Confirmed")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .addField("Server:", capitalize(server), false)
                .addField("Administrator:", event.getInteraction().getUser().getMention(), false)
                .build()).withComponents(Collections.emptyList()).subscribe();
    }

    public static void deny(SelectMenuInteractionEvent event, String server, String uuid) {
        Socket.send(new AdminRequestDenyEvent(server, uuid));

        var data = Database.getPlayerData(uuid);
        if (data == null) return; // Just in case

        event.edit().withEmbeds(EmbedCreateSpec.builder()
                .color(CINNABAR)
                .title("Admin Request Denied")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .addField("Server:", capitalize(server), false)
                .addField("Administrator:", event.getInteraction().getUser().getMention(), false)
                .build()).withComponents(Collections.emptyList()).subscribe();
    }

    public static void confirm(String uuid) {
        var info = netServer.admins.getInfoOptional(uuid);
        if (info == null) return;

        var player = Find.playerByUUID(info.id);
        if (player != null) {
            player.admin(true);
            Bundle.send(player, "commands.login.success");
        }

        netServer.admins.adminPlayer(info.id, player == null ? info.adminUsid : player.usid());
    }

    public static void deny(String uuid) {
        var info = netServer.admins.getInfoOptional(uuid);
        if (info == null) return;

        var player = Find.playerByUUID(info.id);
        if (player != null) {
            player.admin(false);
            Bundle.send(player, "commands.login.fail");
        }

        netServer.admins.unAdminPlayer(info.id);
    }
}