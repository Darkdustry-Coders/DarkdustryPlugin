package darkdustry.discord;

import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import darkdustry.utils.Find;
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

import static arc.util.Strings.format;
import static arc.util.Strings.*;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.format;
import static darkdustry.components.Bundle.*;
import static mindustry.Vars.state;
import static net.dv8tion.jda.api.JDA.Status.CONNECTED;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.entities.Activity.watching;
import static net.dv8tion.jda.api.entities.Message.MentionType.*;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class Bot {

    public static JDA jda;

    public static Role adminRole;
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
            jda.getGuilds().forEach(guild -> guild.getSelfMember().modifyNickname(format("[@], @", discordCommands.getPrefix(), jda.getSelfUser().getName())).queue());

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

    public static void sendMessageToGame(Member member, Message message) {
        DarkdustryPlugin.discord("@: @", member.getEffectiveName(), message.getContentDisplay());

        var roles = member.getRoles();
        var reply = message.getReferencedMessage();

        Groups.player.each(player -> {
            var locale = Find.locale(player.locale);

            bundled(player, "discord.chat",
                    Integer.toHexString(member.getColorRaw()),
                    roles.isEmpty() ? format("discord.chat.no-role", locale) : roles.get(0).getName(),
                    member.getEffectiveName(),
                    reply != null ? format("discord.chat.reply", locale, reply.getAuthor().getName()) : "",
                    message.getContentDisplay());
        });
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(ADMINISTRATOR));
    }

    public static void updateBotStatus() {
        if (connected())
            jda.getPresence().setActivity(watching("at " + Groups.player.size() + " players on " + stripColors(state.map.name())));
    }

    public static void sendMessage(MessageChannel channel, String text, Object... values) {
        if (channel != null && channel.canTalk())
            channel.sendMessage(format(text, values)).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String title, Object... values) {
        if (channel != null && channel.canTalk())
            channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(format(title, values)).build()).queue();
    }

    public static EmbedBuilder embed(Color color, String title) {
        return new EmbedBuilder().setColor(color).setTitle(title);
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
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.success).setTitle(format(title, values)).build());
        }

        public MessageCreateAction info(String title, String description, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(title).setDescription(format(description, values)).build());
        }

        public MessageCreateAction info(String title, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(format(title, values)).build());
        }

        public MessageCreateAction info(String title, String description, FileUpload image, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.info).setTitle(title).setDescription(format(description, values)).setImage("attachment://" + image.getName()).build()).addFiles(image);
        }

        public MessageCreateAction error(String title, String description, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.error).setTitle(title).setDescription(format(description, values)).build());
        }

        public MessageCreateAction error(String title, Object... values) {
            return message.replyEmbeds(new EmbedBuilder().setColor(Palette.error).setTitle(format(title, values)).build());
        }
    }
}