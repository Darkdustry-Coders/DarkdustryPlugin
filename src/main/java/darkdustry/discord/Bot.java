package darkdustry.discord;

import arc.util.Log;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Authme;
import discord4j.common.ReactorResources;
import discord4j.common.retry.ReconnectOptions;
import discord4j.common.util.Snowflake;
import discord4j.core.*;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.presence.*;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.OrderUtil;
import discord4j.gateway.intent.*;
import discord4j.rest.util.AllowedMentions;
import mindustry.gen.Groups;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.netty.http.HttpResources;
import reactor.netty.resources.LoopResources;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
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
            // d4j либо в rest, либо в websocket клиенте использует глобальные ресурсы, поэтому лучше их заменить
            HttpResources.set(LoopResources.create("d4j-http", 4, true));

            gateway = DiscordClientBuilder.create(config.discordBotToken)
                    .setDefaultAllowedMentions(AllowedMentions.suppressAll())
                    .setReactorResources(ReactorResources.builder()
                            .timerTaskScheduler(Schedulers.newParallel("d4j-parallel", 4, true))
                            .build())
                    .build()
                    .gateway()
                    .setReconnectOptions(ReconnectOptions.builder()
                            .setBackoffScheduler(Schedulers.newParallel("d4j-backoff", 4, true))
                            .build())
                    .setEventDispatcher(EventDispatcher.builder()
                            .eventScheduler(ForkJoinPoolScheduler.create("d4j-events", 4))
                            .build())
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

                                case valid -> Log.info("[Discord] @ used @", member.getDisplayName(), message.getContent());
                            }
                        });

                // Prevent commands from being sent to the game
                if (message.getContent().startsWith(config.discordBotPrefix) || !message.getChannelId().equals(botChannel.getId())) return;

                var roles = event.getClient()
                        .getGuildRoles(member.getGuildId())
                        .filter(role -> member.getRoleIds().contains(role.getId()))
                        .sort(OrderUtil.ROLE_ORDER)
                        .cache();

                roles.takeLast(1)
                        .singleOrEmpty()
                        .zipWith(roles.map(Role::getColor)
                                .filter(color -> !color.equals(Role.DEFAULT_COLOR))
                                .last(Role.DEFAULT_COLOR))
                        .switchIfEmpty(Mono.fromRunnable(() -> {
                            Log.info("[Discord] @: @", member.getDisplayName(), message.getContent());
                            Bundle.send("discord.chat", member.getDisplayName(), message.getContent());
                        })).subscribe(TupleUtils.consumer((role, color) -> {
                            Log.info("[Discord] @ | @: @", role.getName(), member.getDisplayName(), message.getContent());
                            Bundle.send("discord.chat.role", Integer.toHexString(color.getRGB()), role.getName(), member.getDisplayName(), message.getContent());
                        }));
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