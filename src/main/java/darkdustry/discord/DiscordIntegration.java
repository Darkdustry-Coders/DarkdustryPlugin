package darkdustry.discord;

import darkdustry.config.Config.Gamemode;
import darkdustry.database.Database;
import darkdustry.database.models.*;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.Find;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.*;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import useful.Bundle;

import java.time.Instant;
import java.util.Collections;

import static arc.Core.*;
import static darkdustry.discord.DiscordBot.*;
import static discord4j.common.util.TimestampFormat.*;
import static mindustry.Vars.*;

public class DiscordIntegration {

    public static void sendBan(String server, Ban ban) {
        if (!connected) return;

        banChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.CINNABAR)
                .title("Ban")
                .addField("Player:", ban.playerName + " [" + ban.playerID + "]", false)
                .addField("Admin:", ban.adminName, false)
                .addField("Reason:", ban.reason, false)
                .addField("Unban Date:", LONG_DATE.format(ban.unbanDate.toInstant()), false)
                .footer(Gamemode.valueOf(server).displayName, null)
                .timestamp(Instant.now()).build()).subscribe();
    }

    public static void sendVoteKick(String server, String targetName, int targetID, String initiatorName, int initiatorID, String reason, String votesFor, String votesAgainst) {
        if (!connected) return;

        votekickChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.MOON_YELLOW)
                .title("Vote Kick")
                .addField("Target:", targetName + " [" + targetID + "]", false)
                .addField("Initiator:", initiatorName + " [" + initiatorID + "]", false)
                .addField("Reason:", reason, false)
                .addField("Votes For:", votesFor, false)
                .addField("Votes Against:", votesAgainst, false)
                .footer(Gamemode.valueOf(server).displayName, null)
                .timestamp(Instant.now()).build()).subscribe();
    }

    public static void sendAdminRequest(String server, PlayerData data) {
        if (!connected) return;

        adminChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.BISMARK)
                .title("Admin Request")
                .description("Select the desired option to confirm or deny the request. Confirm only your requests!")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .footer(Gamemode.valueOf(server).displayName, null)
                .timestamp(Instant.now())
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
                .color(Color.MEDIUM_SEA_GREEN)
                .title("Admin Request Confirmed")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .addField("Administrator:", event.getInteraction().getUser().getMention(), false)
                .footer(server, null)
                .timestamp(Instant.now())
                .build()).withComponents(Collections.emptyList()).subscribe();
    }

    public static void deny(SelectMenuInteractionEvent event, String server, String uuid) {
        Socket.send(new AdminRequestDenyEvent(server, uuid));

        var data = Database.getPlayerData(uuid);
        if (data == null) return; // Just in case

        event.edit().withEmbeds(EmbedCreateSpec.builder()
                .color(Color.CINNABAR)
                .title("Admin Request Denied")
                .addField("Player:", data.plainName(), false)
                .addField("ID:", String.valueOf(data.id), false)
                .addField("Administrator:", event.getInteraction().getUser().getMention(), false)
                .footer(server, null)
                .timestamp(Instant.now())
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

    public static void updateActivity() {
        if (connected)
            updateActivity("at " + settings.getInt("totalPlayers", Groups.player.size()) + " players on Darkdustry");
    }

    public static void updateActivity(String activity) {
        if (connected)
            gateway.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching(activity))).subscribe();
    }

    public static void sendMessage(long channelId, String message) {
        if (connected)
            gateway.getChannelById(Snowflake.of(channelId))
                    .ofType(GuildMessageChannel.class)
                    .flatMap(channel -> channel.createMessage(message))
                    .subscribe();
    }

    public static void sendMessageEmbed(long channelId, EmbedCreateSpec embed) {
        if (connected)
            gateway.getChannelById(Snowflake.of(channelId))
                    .ofType(GuildMessageChannel.class)
                    .flatMap(channel -> channel.createMessage(embed))
                    .subscribe();
    }
}