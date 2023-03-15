package darkdustry.discord;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.AuthMe;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import mindustry.gen.Groups;
import reactor.core.publisher.Mono;

import static darkdustry.PluginVars.config;
import static darkdustry.PluginVars.discordCommands;
import static darkdustry.utils.Checks.noRole;
import static darkdustry.utils.Utils.stripDiscord;
import static mindustry.Vars.state;
import static useful.Bundle.sendToChat;

public class Bot {

    public static GatewayDiscordClient gateway;

    public static GuildMessageChannel botChannel, adminChannel;
    public static Role adminRole, mapReviewerRole;

    public static void connect() {
        try {
            gateway = DiscordClientBuilder.create(config.discordBotToken)
                    .setDefaultAllowedMentions(AllowedMentions.suppressAll())
                    .build()
                    .gateway()
                    .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES))
                    .login()
                    .block();

            botChannel = gateway.getChannelById(Snowflake.of(config.discordBotChannelId)).ofType(GuildMessageChannel.class).block();
            adminChannel = gateway.getChannelById(Snowflake.of(config.discordAdminChannelId)).ofType(GuildMessageChannel.class).block();

            adminRole = gateway.getRoleById(Snowflake.of(config.discordBotGuildId), Snowflake.of(config.discordAdminRoleId)).block();
            // mapReviewerRole = gateway.getRoleById(Snowflake.of(config.discordGuildId), Snowflake.of(config.discordMapReviewerRoleId)).block();

            gateway.on(MessageCreateEvent.class).subscribe(event -> {
                var message = event.getMessage();
                if (message.getContent().isEmpty()) return;

                var member = event.getMember().orElse(null);
                if (member == null || member.isBot()) return;

                message.getChannel()
                        .map(channel -> new MessageContext(message, member, channel))
                        .flatMap(context -> {
                            var response = discordCommands.handleMessage(context.message().getContent(), context);
                            return switch (response.type) {
                                case fewArguments ->
                                        context.error("Too Few Arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText);
                                case manyArguments ->
                                        context.error("Too Many Arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText);
                                case unknownCommand ->
                                        context.error("Unknown Command", "To see a list of all available commands, use @**help**", discordCommands.prefix);
                                default -> Mono.empty();
                            };
                        })
                        .subscribe();

                if (!message.getChannelId().equals(botChannel.getId())) return;

                member.getHighestRole().switchIfEmpty(Mono.fromRunnable(() -> {
                    Log.info("[Discord] @: @", member.getDisplayName(), message.getContent());
                    sendToChat("discord.chat.no-role", member.getDisplayName(), message.getContent());
                })).subscribe(role -> {
                    Log.info("[Discord] @ | @: @", role.getName(), member.getDisplayName(), message.getContent());
                    sendToChat("discord.chat", Integer.toHexString(role.getColor().getRGB()), role.getName(), member.getDisplayName(), message.getContent());
                });
            });

            gateway.on(SelectMenuInteractionEvent.class).subscribe(event -> {
                if (noRole(event, adminRole)) return;

                switch (event.getValues().get(0)) {
                    case "confirm" -> AuthMe.confirm(event);
                    case "deny" -> AuthMe.deny(event);
                    case "info" -> AuthMe.info(event);
                }
            });

            gateway.getSelf()
                    .flatMap(user -> gateway.getGuilds()
                            .flatMap(guild -> guild.changeSelfNickname("[" + config.discordBotPrefix + "] " + user.getUsername()))
                            .then()).subscribe();

            discordCommands = new CommandHandler(config.discordBotPrefix);
            discordCommands.<MessageContext>register("help", "Get helped", (args, context) -> {
                context.info("блять", "иди нахуй").subscribe();
            });
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect bot: @", e);
        }
    }

    public static void exit() {
        gateway.logout().subscribe();
    }

    public static void updateActivity() {
        updateActivity("at " + Groups.player.size() + " players on " + state.map.name());
    }

    public static void updateActivity(String activity) {
        gateway.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching(activity))).subscribe();
    }

    public static void sendMessage(GuildMessageChannel channel, String text) {
        channel.createMessage(text).subscribe();
    }

    public static void sendMessage(GuildMessageChannel channel, String name, String message) {
        sendMessage(channel, Strings.format("`@: @`", stripDiscord(name), stripDiscord(message)));
    }

    public static void sendMessage(GuildMessageChannel channel, EmbedCreateSpec embed) {
        channel.createMessage(embed).subscribe();
    }
}