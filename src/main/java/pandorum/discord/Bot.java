package pandorum.discord;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import arc.util.Strings;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import pandorum.commands.DiscordCommandsLoader;
import pandorum.comp.Authme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.loginWaiting;

public class Bot {

    public static final CommandHandler discordHandler = new CommandHandler(config.discordBotPrefix);
    public static final Color normalColor = Color.ORANGE, successColor = Color.GREEN, errorColor = Color.RED;

    public static DiscordClient discordClient;
    public static GatewayDiscordClient gatewayDiscordClient;

    public static MessageChannel botChannel, adminChannel;

    public static void init() {
        try {
            discordClient = DiscordClientBuilder.create(config.discordBotToken).onClientResponse(ResponseFunction.emptyIfNotFound()).onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400)).setDefaultAllowedMentions(AllowedMentions.suppressAll()).build();
            gatewayDiscordClient = discordClient.login().block();

            gatewayDiscordClient.on(MessageCreateEvent.class).subscribe(event -> {
                Message message = event.getMessage();
                Member member = message.getAuthorAsMember().block();
                handleMessage(message);

                if (message.getChannelId().equals(botChannel.getId()) && member != null && !member.isBot() && !message.getContent().isBlank()) {
                    sendToChat("events.discord.chat", member.getDisplayName(), message.getContent());
                    Log.info("[Discord] @: @", member.getDisplayName(), message.getContent());
                }
            }, e -> {});

            gatewayDiscordClient.on(ButtonInteractionEvent.class).subscribe(event -> {
                Message message = event.getMessage().get();
                if (loginWaiting.containsKey(message)) {
                    switch (event.getCustomId()) {
                        case "confirm" -> Authme.confirm(message, event);
                        case "deny" -> Authme.deny(message, event);
                        case "check" -> Authme.check(message, event);
                    }
                }
            }, e -> {});

            botChannel = getChannelByID(config.discordBotChannelID);
            adminChannel = getChannelByID(config.discordAdminChannelID);
            DiscordCommandsLoader.registerDiscordCommands(discordHandler);

            Log.info("[Darkdustry] Бот успешно запущен...");
        } catch (Exception e) {
            Log.err("[Darkdustry] Ошибка запуска бота...");
            Log.err(e);
        }
    }

    public static void handleMessage(Message message) {
        CommandResponse response = discordHandler.handleMessage(message.getContent(), message);
        if (response.type == ResponseType.fewArguments || response.type == ResponseType.manyArguments) {
            err(message, "Неверное количество аргументов.", "Использование : **@@** @", discordHandler.getPrefix(), response.command.text, response.command.paramText);
        }
    }

    public static boolean adminCheck(Member member) {
        if (member == null || member.isBot()) return true;
        return member.getRoles().toStream().noneMatch(role -> role.getId().equals(Snowflake.of(config.discordAdminRoleID)));
    }

    public static MessageChannel getChannelByID(long id) {
        return (MessageChannel) gatewayDiscordClient.getChannelById(Snowflake.of(id)).block();
    }

    /**
     * Различные методы для отправки сообщений и эмбедов.
     */

    public static void updateBotStatus() {
        gatewayDiscordClient.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching(Strings.format("Игроков на сервере: @", Groups.player.size())))).subscribe(null, e -> {});
    }

    public static void text(MessageChannel channel, String text, Object... args) {
        channel.createMessage(Strings.format(text, args)).subscribe(null, e -> {});
    }

    public static void text(String text, Object... args) {
        text(botChannel, text, args);
    }

    public static void text(Message message, String text, Object... args) {
        text(Objects.requireNonNull(message.getChannel().block()), text, args);
    }

    public static void sendEmbed(MessageChannel channel, EmbedCreateSpec embed) {
        channel.createMessage(embed).subscribe(null, e -> {});
    }

    public static void sendEmbed(EmbedCreateSpec embed) {
        sendEmbed(botChannel, embed);
    }

    public static void sendEmbed(Message message, EmbedCreateSpec embed) {
        sendEmbed(Objects.requireNonNull(message.getChannel().block()), embed);
    }

    public static void info(MessageChannel channel, String title, String text, Object... args) {
        sendEmbed(channel, EmbedCreateSpec.builder().color(normalColor).addField(title, Strings.format(text, args), true).build());
    }

    public static void info(Message message, String title, String text, Object... args) {
        info(message.getChannel().block(), title, text, args);
    }

    public static void err(MessageChannel channel, String title, String text, Object... args) {
        sendEmbed(channel, EmbedCreateSpec.builder().color(errorColor).addField(title, Strings.format(text, args), true).build());
    }

    public static void err(Message message, String title, String text, Object... args) {
        err(message.getChannel().block(), title, text, args);
    }

    public static void sendFile(MessageChannel channel, Fi file) throws FileNotFoundException {
        channel.createMessage(MessageCreateSpec.builder().addFile(MessageCreateFields.File.of(file.name(), new FileInputStream(file.file()))).build()).subscribe(null, e -> {});
    }

    public static void sendFile(Message message, Fi file) throws FileNotFoundException {
        sendFile(Objects.requireNonNull(message.getChannel().block()), file);
    }
}
