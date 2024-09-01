package darkdustry.discord;

import darkdustry.config.Config.Gamemode;
import darkdustry.database.Database;
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

    public static void sendBan(BanEvent event) {
        if (!connected) return;

        banChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.CINNABAR)
                .title("Ban")
                .addField("Player:", event.ban().playerName + " [" + event.ban().playerID + "]", false)
                .addField("Admin:", event.ban().adminName, false)
                .addField("Reason:", event.ban().reason, false)
                .addField("Unban Date:", LONG_DATE.format(event.ban().unbanDate.toInstant()), false)
                .footer(Gamemode.getDisplayName(event.server()), null)
                .timestamp(Instant.now()).build()).subscribe();
    }

    public static void sendMute(MuteEvent event) {
        if (!connected) return;

        muteChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Mute")
                .addField("Player:", event.mute().playerName + " [" + event.mute().playerID + "]", false)
                .addField("Admin:", event.mute().adminName, false)
                .addField("Reason:", event.mute().reason, false)
                .addField("Unmute Date:", LONG_DATE.format(event.mute().unmuteDate.toInstant()), false)
                .footer(Gamemode.getDisplayName(event.server()), null)
                .timestamp(Instant.now()).build()).subscribe();
    }

    public static void sendVoteKick(VoteKickEvent event) {
        if (!connected) return;

        votekickChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.MOON_YELLOW)
                .title("Vote Kick")
                .addField("Target:", event.target(), false)
                .addField("Initiator:", event.initiator(), false)
                .addField("Reason:", event.reason(), false)
                .addField("Votes For:", event.votesFor(), false)
                .addField("Votes Against:", event.votesAgainst(), false)
                .footer(Gamemode.getDisplayName(event.server()), null)
                .timestamp(Instant.now()).build()).subscribe();
    }

    public static void sendAdminRequest(AdminRequestEvent event) {
        if (!connected) return;

        adminChannel.createMessage(EmbedCreateSpec.builder()
                .color(Color.BISMARK)
                .title("Admin Request")
                .description("Select the desired option to confirm or deny the request. Confirm only your requests!")
                .addField("Player:", event.data().plainName(), false)
                .addField("ID:", String.valueOf(event.data().id), false)
                .footer(Gamemode.getDisplayName(event.server()), null)
                .timestamp(Instant.now())
                .build()
        ).withComponents(ActionRow.of(SelectMenu.of("admin-request",
                Option.of("Confirm", "confirm-" + event.server() + "-" + event.data().uuid).withDescription("Confirm this request.").withEmoji(ReactionEmoji.unicode("✅")),
                Option.of("Deny", "deny-" + event.server() + "-" + event.data().uuid).withDescription("Deny this request.").withEmoji(ReactionEmoji.unicode("❌"))
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
                .footer(Gamemode.getDisplayName(server), null)
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
                .footer(Gamemode.getDisplayName(server), null)
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