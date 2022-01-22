package pandorum.discord;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Strings;
import arc.util.Timer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import mindustry.gen.Groups;
import pandorum.commands.DiscordCommandsLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static pandorum.PluginVars.config;
import static pandorum.discord.BotMain.gatewayDiscordClient;

public class BotHandler {

    public static final CommandHandler discordHandler = new CommandHandler(config.discordBotPrefix);
    public static final Color normalColor = Color.ORANGE, successColor = Color.GREEN, errorColor = Color.RED;

    public static MessageChannel botChannel, adminChannel;

    public static void init() {
        DiscordCommandsLoader.registerDiscordCommands(discordHandler);

        botChannel = (MessageChannel) gatewayDiscordClient.getChannelById(Snowflake.of(config.discordBotChannelID)).block();
        adminChannel = (MessageChannel) gatewayDiscordClient.getChannelById(Snowflake.of(config.discordAdminChannelID)).block();

        Timer.schedule(() -> gatewayDiscordClient.updatePresence(ClientPresence.of(Status.ONLINE, ClientActivity.watching("Игроков на сервере: " + Groups.player.size()))).subscribe(null, e -> {}), 0f, 1f);
    }

    public static void handleMessage(Message message) {
        CommandResponse response = discordHandler.handleMessage(message.getContent(), message);
        if (response.type == ResponseType.fewArguments || response.type == ResponseType.manyArguments) {
            err(message.getChannel().block(), "Неверное количество аргументов.", "Использование : **@@** @", discordHandler.getPrefix(), response.command.text, response.command.paramText);
        }
    }

    public static void text(String text, Object... args) {
        text(botChannel, text, args);
    }

    public static void text(MessageChannel channel, String text, Object... args) {
        if (channel != null) channel.createMessage(Strings.format(text, args)).subscribe(null, e -> {});
    }

    public static void info(MessageChannel channel, String title, String text, Object... args) {
        sendEmbed(channel, EmbedCreateSpec.builder().color(normalColor).addField(title, Strings.format(text, args), true).build());
    }

    public static void err(MessageChannel channel, String title, String text, Object... args) {
        sendEmbed(channel, EmbedCreateSpec.builder().color(errorColor).addField(title, Strings.format(text, args), true).build());
    }

    public static void sendEmbed(EmbedCreateSpec embed) {
        sendEmbed(botChannel, embed);
    }

    public static void sendEmbed(MessageChannel channel, EmbedCreateSpec embed) {
        if (channel != null) channel.createMessage(embed).subscribe(null, e -> {});
    }

    public static void sendFile(MessageChannel channel, Fi file) throws FileNotFoundException {
        if (channel != null) channel.createMessage(MessageCreateSpec.builder().addFile(MessageCreateFields.File.of(file.name(), new FileInputStream(file.file()))).build()).subscribe(null, e -> {});
    }

    public static boolean adminCheck(Member member) {
        if (member == null || member.isBot()) return true;
        return member.getRoles().toStream().noneMatch(role -> role.getId().equals(Snowflake.of(config.discordAdminRoleID)));
    }
}
