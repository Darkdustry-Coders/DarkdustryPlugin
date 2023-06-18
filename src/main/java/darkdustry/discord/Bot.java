package darkdustry.discord;

import arc.util.Log;
import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import darkdustry.features.Authme;
import discord4j.common.util.Snowflake;
import discord4j.core.*;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.*;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.gateway.intent.*;
import discord4j.rest.util.AllowedMentions;
import mindustry.gen.Groups;
import reactor.core.publisher.Mono;
import useful.Bundle;

import static arc.Core.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class Bot {

    public static GatewayDiscordClient gateway;

    public static GuildMessageChannel botChannel, banChannel, adminChannel;
    public static Role adminRole, mapReviewerRole;

    public static boolean connected;

    public static void connect() {
        try {
            gateway = DiscordClientBuilder.create(config.discordBotToken)
                    .setDefaultAllowedMentions(AllowedMentions.suppressAll())
                    .build()
                    .gateway()
                    .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES))
                    .login()
                    .blockOptional()
                    .orElseThrow();

            botChannel = gateway.getChannelById(Snowflake.of(config.discordBotChannelId)).ofType(GuildMessageChannel.class).block();
            banChannel = gateway.getChannelById(Snowflake.of(config.discordBanChannelId)).ofType(GuildMessageChannel.class).block();
            adminChannel = gateway.getChannelById(Snowflake.of(config.discordAdminChannelId)).ofType(GuildMessageChannel.class).block();

            adminRole = gateway.getRoleById(Snowflake.of(config.discordBotGuildId), Snowflake.of(config.discordAdminRoleId)).block();
            mapReviewerRole = gateway.getRoleById(Snowflake.of(config.discordBotGuildId), Snowflake.of(config.discordMapReviewerRoleId)).block();

            gateway.on(MessageCreateEvent.class).subscribe(event -> {
                var message = event.getMessage();
                if (message.getContent().isEmpty()) return;

                var member = event.getMember().orElse(null);
                if (member == null || member.isBot()) return;

                message.getChannel()
                        .map(channel -> new MessageContext(message, member, channel))
                        .subscribe(context -> {
                            var response = discordCommands.handleMessage(message.getContent(), context);
                            switch (response.type) {
                                case fewArguments -> context.error("Too Few Arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText).subscribe();
                                case manyArguments -> context.error("Too Many Arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText).subscribe();
                                case unknownCommand -> context.error("Unknown Command", "To see a list of all available commands, use @**help**", discordCommands.prefix).subscribe();
                            }
                        });

                if (!message.getChannelId().equals(botChannel.getId())) return;

                member.getHighestRole().switchIfEmpty(Mono.fromRunnable(() -> {
                    Log.info("[Discord] @: @", member.getDisplayName(), message.getContent());
                    Bundle.send("discord.chat", member.getDisplayName(), message.getContent());
                })).subscribe(role -> {
                    Log.info("[Discord] @ | @: @", role.getName(), member.getDisplayName(), message.getContent());
                    Bundle.send("discord.chat.role", Integer.toHexString(role.getColor().getRGB()), role.getName(), member.getDisplayName(), message.getContent());
                });
            });

            gateway.on(SelectMenuInteractionEvent.class).subscribe(event -> {
                if (noRole(event, adminRole)) return;

                switch (event.getValues().get(0)) {
                    case "confirm" -> Authme.confirm(event);
                    case "deny" -> Authme.deny(event);
                    case "info" -> Authme.info(event);
                }
            });

            gateway.getSelf()
                    .flatMap(user -> gateway.getGuilds()
                            .flatMap(guild -> guild.changeSelfNickname("[" + config.discordBotPrefix + "] " + user.getUsername()))
                            .then()
                    ).subscribe();

            connected = true;

            DiscordCommands.load();
            DarkdustryPlugin.info("Bot connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect bot: @", e);
        }
    }

    public static void updateActivity() {
        if (connected)
            updateActivity("at " + settings.getInt("totalPlayers", Groups.player.size()) + " players on " + state.map.plainName());
    }

    public static void updateActivity(String activity) {
        if (connected)
            gateway.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching(activity))).subscribe();
    }

    public static void sendMessage(String name, String message) {
        if (connected)
            botChannel.createMessage("`" + stripDiscord(name) + ": " + stripDiscord(message) + "`").subscribe();
    }

    public static void sendMessage(EmbedCreateSpec embed) {
        if (connected)
            botChannel.createMessage(embed).subscribe();
    }
}