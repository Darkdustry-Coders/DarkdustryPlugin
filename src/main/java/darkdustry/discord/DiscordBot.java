package darkdustry.discord;

import arc.util.*;
import darkdustry.features.net.Socket;
import darkdustry.listeners.SocketEvents.*;
import darkdustry.utils.PageIterator;
import discord4j.common.ReactorResources;
import discord4j.common.retry.ReconnectOptions;
import discord4j.common.util.Snowflake;
import discord4j.core.*;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.OrderUtil;
import discord4j.gateway.intent.*;
import discord4j.rest.util.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.netty.http.HttpResources;
import reactor.netty.resources.LoopResources;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.util.function.Predicate;

import static darkdustry.PluginVars.*;
import static darkdustry.config.DiscordConfig.*;
import static darkdustry.utils.Checks.*;

public class DiscordBot {

    public static GatewayDiscordClient gateway;

    public static GuildMessageChannel banChannel, adminChannel;
    public static Role adminRole, mapReviewerRole;

    public static boolean connected;

    public static void connect() {
        try {
            // d4j либо в rest, либо в websocket клиенте использует глобальные ресурсы, поэтому лучше их заменить
            HttpResources.set(LoopResources.create("d4j-http", 4, true));

            gateway = DiscordClientBuilder.create(discordConfig.token)
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

            banChannel = gateway.getChannelById(Snowflake.of(discordConfig.banChannelId)).ofType(GuildMessageChannel.class).block();
            adminChannel = gateway.getChannelById(Snowflake.of(discordConfig.adminChannelId)).ofType(GuildMessageChannel.class).block();

            adminRole = gateway.getRoleById(Snowflake.of(discordConfig.botGuildId), Snowflake.of(discordConfig.adminRoleId)).block();
            mapReviewerRole = gateway.getRoleById(Snowflake.of(discordConfig.botGuildId), Snowflake.of(discordConfig.mapReviewerRoleId)).block();

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
                if (message.getContent().startsWith(discordConfig.prefix)) return;

                var server = discordConfig.serverToChannel.findKey(message.getChannelId().asLong(), false);
                if (server == null) return;

                var roles = event.getClient()
                        .getGuildRoles(member.getGuildId())
                        .filter(role -> member.getRoleIds().contains(role.getId()))
                        .sort(OrderUtil.ROLE_ORDER)
                        .cache();

                roles.takeLast(1)
                        .singleOrEmpty()
                        .zipWith(roles.map(Role::getColor)
                                .filter(Predicate.not(Predicate.isEqual(Role.DEFAULT_COLOR)))
                                .last(Color.WHITE))
                        .switchIfEmpty(Mono.fromRunnable(() ->
                                Socket.send(new DiscordMessageEvent(server, member.getDisplayName(), message.getContent()))))
                        .subscribe(TupleUtils.consumer((role, color) ->
                                Socket.send(new DiscordMessageEvent(server, role.getName(), Integer.toHexString(color.getRGB()), member.getDisplayName(), message.getContent()))));
            });

            gateway.on(ButtonInteractionEvent.class).subscribe(event -> {
                var content = event.getCustomId().split("-", 3);
                if (content.length < 3) return;

                Socket.request(new ListRequest(content[0], content[1], Strings.parseInt(content[2])), response -> {
                    var embed = EmbedCreateSpec.builder();

                    switch (content[0]) {
                        case "maps" -> PageIterator.formatMapsPage(embed, response);
                        case "players" -> PageIterator.formatPlayersPage(embed, response);

                        default -> throw new IllegalStateException();
                    }

                    event.edit().withEmbeds(embed.build()).withComponents(PageIterator.createPageButtons(content[0], content[1], response)).subscribe();
                });
            });

            gateway.on(SelectMenuInteractionEvent.class).subscribe(event -> {
                if (noRole(event, adminRole)) return;

                if (event.getCustomId().equals("admin-request")) {
                    var content = event.getValues().getFirst().split("-", 3);
                    if (content.length < 3) return;

                    switch (content[0]) {
                        case "confirm" -> DiscordIntegration.confirm(event, content[1], content[2]);
                        case "deny" -> DiscordIntegration.deny(event, content[1], content[2]);
                    }
                }
            });

            gateway.getSelf()
                    .flatMap(user -> gateway.getGuilds()
                            .flatMap(guild -> guild.changeSelfNickname("[" + discordConfig.prefix + "] " + user.getUsername()))
                            .then()
                    ).subscribe();

            connected = true;

            Log.info("Bot connected.");
        } catch (Exception e) {
            Log.err("Failed to connect bot", e);
        }
    }
}