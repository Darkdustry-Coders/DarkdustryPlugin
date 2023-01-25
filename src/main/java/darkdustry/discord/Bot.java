package darkdustry.discord;

import arc.util.Strings;
import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import java.awt.Color;
import java.util.EnumSet;

import static arc.util.CommandHandler.ResponseType.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.stripDiscord;
import static mindustry.Vars.state;
import static net.dv8tion.jda.api.JDA.Status.CONNECTED;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.entities.Activity.watching;
import static net.dv8tion.jda.api.entities.Message.MentionType.*;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static useful.Bundle.sendToChat;

public class Bot {

    public static JDA jda;

    public static Role adminRole, mapReviewerRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListeners()).build().awaitReady();

            adminRole = jda.getRoleById(config.discordAdminRoleId);
            botChannel = jda.getTextChannelById(config.discordBotChannelId);
            adminChannel = jda.getTextChannelById(config.discordAdminChannelId);

            RestActionImpl.setDefaultFailure(null); // Ignore all errors in RestActions
            MessageRequest.setDefaultMentions(EnumSet.of(CHANNEL, EMOJI));

            DiscordCommands.load();
            updateBotStatus();

            // Изменяем никнейм на [prefix] Name
            jda.getGuildCache()
                    .stream()
                    .findFirst()
                    .ifPresent(guild -> guild.getSelfMember().modifyNickname(Strings.format("[@] @", discordCommands.prefix, jda.getSelfUser().getName())).queue());

            DarkdustryPlugin.info("Bot connected. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect bot: @", e);
        }
    }

    public static boolean connected() {
        return jda != null && jda.getStatus() == CONNECTED;
    }

    public static void exit() {
        if (connected())
            jda.shutdown();
    }

    public static boolean handleMessage(Context context) {
        var response = discordCommands.handleMessage(context.message.getContentRaw(), context);

        if (response.type == manyArguments)
            context.error(":interrobang: Too many arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText).queue();
        else if (response.type == fewArguments)
            context.error(":interrobang: Too few arguments", "Usage: @**@** @", discordCommands.prefix, response.runCommand, response.command.paramText).queue();

        return response.type != noCommand;
    }

    public static void sendMessageToGame(Member member, Message message) {
        DarkdustryPlugin.discord("@: @", member.getEffectiveName(), message.getContentDisplay());
        sendToChat("discord.chat", Integer.toHexString(member.getColorRaw()), !member.getRoles().isEmpty() ? member.getRoles().get(0).getName() : "No Role", member.getEffectiveName(), message.getContentDisplay());
    }

    public static boolean isAdmin(Member member) {
        return member.getRoles().contains(adminRole) || member.hasPermission(ADMINISTRATOR);
    }

    public static boolean isMapReviewer(Member member) {
        return isAdmin(member) || member.getRoles().contains(mapReviewerRole);
    }

    public static void updateBotStatus() {
        if (connected())
            jda.getPresence().setActivity(watching("at " + Groups.player.size() + " players on " + Strings.stripColors(state.map.name())));
    }

    public static void sendMessage(MessageChannel channel, String name, String message) {
        if (channel != null && channel.canTalk())
            channel.sendMessage(Strings.format("`@: @`", stripDiscord(name), stripDiscord(message))).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String title, Object... values) {
        if (channel != null && channel.canTalk())
            channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(Strings.format(title, values)).build()).queue();
    }

    public static class Palette {
        public static final Color
                success = Color.decode("#3cfb63"),
                info = Color.decode("#fcf47c"),
                error = Color.decode("#f93c3c");
    }

    public record Context(MessageReceivedEvent event, Message message) {
        public Context(MessageReceivedEvent event) {
            this(event, event.getMessage());
        }

        public MessageCreateAction success(String title, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.success).setTitle(Strings.format(title, values)).build());
        }

        public MessageCreateAction info(String title, String description, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(title).setDescription(Strings.format(description, values)).build());
        }

        public MessageCreateAction info(String title, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(Strings.format(title, values)).build());
        }

        public MessageCreateAction info(String title, String description, FileUpload image, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(title).setDescription(Strings.format(description, values)).setImage("attachment://" + image.getName()).build()).addFiles(image);
        }

        public MessageCreateAction error(String title, String description, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.error).setTitle(title).setDescription(Strings.format(description, values)).build());
        }

        public MessageCreateAction error(String title, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.error).setTitle(Strings.format(title, values)).build());
        }
    }
}