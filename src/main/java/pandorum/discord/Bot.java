package pandorum.discord;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.AllowedMentions;
import pandorum.Loader;
import pandorum.components.Bundle;

import java.awt.*;
import java.util.EnumSet;
import java.util.Locale;

import static pandorum.PluginVars.*;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.Search.findLocale;

public class Bot {

    public static JDA jda;

    public static Guild botGuild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            JDABuilder builder = JDABuilder.createLight(config.discordBotToken)
                    .addEventListeners(new MessageListener())
                    .addEventListeners(new ButtonListener());

            jda = builder.build();
            jda.awaitReady();

            botGuild = jda.getGuildById(config.discordGuildId);
            assert botGuild != null;

            adminRole = botGuild.getRoleById(config.discordAdminRoleId);
            botChannel = botGuild.getTextChannelById(config.discordBotChannelId);
            adminChannel = botGuild.getTextChannelById(config.discordAdminChannelId);
            assert adminRole != null && botChannel != null && adminChannel != null;

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(MentionType.class));

            botGuild.getSelfMember().modifyNickname("[" + config.discordBotPrefix + "] " + jda.getSelfUser().getName()).queue();

            discordCommands = new CommandHandler(config.discordBotPrefix);
            Loader.registerDiscordCommands(discordCommands);

            updateBotStatus(0);

            Log.info("[Darkdustry] Бот успешно подключен. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception e) {
            Log.err("[Darkdustry] Не удалось запустить бота", e);
        }
    }

    public static void handleMessage(MessageContext context) {
        CommandResponse response = discordCommands.handleMessage(context.message.getContentRaw(), context);

        if (response.type == ResponseType.unknownCommand) {
            context.err(":interrobang: Неизвестная команда.", "Используй: **@help**, чтобы получить список доступных команд.", discordCommands.getPrefix());
        } else if (response.type == ResponseType.fewArguments) {
            context.err(":interrobang: Слишком мало аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
        } else if (response.type == ResponseType.manyArguments) {
            context.err(":interrobang: Слишком много аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
        }
    }

    public static void sendMessageToGame(MessageContext context) {
        if (context.channel != botChannel || context.message.getContentDisplay().length() == 0) return;

        Log.info("[Discord] @: @", context.member.getEffectiveName(), context.message.getContentDisplay());

        var roles = context.member.getRoles();
        var reply = context.message.getReferencedMessage();

        Groups.player.each(player -> {
            Locale locale = findLocale(player.locale);

            bundled(player, "discord.chat",
                    Integer.toHexString(context.member.getColorRaw()),
                    roles.isEmpty() ? Bundle.get("discord.chat.no-role", locale) : roles.get(0).getName(),
                    context.member.getEffectiveName(),
                    reply != null ? Bundle.format("discord.chat.reply", locale, botGuild.retrieveMember(reply.getAuthor()).complete().getEffectiveName()) : "",
                    context.message.getContentDisplay()
            );
        });
    }

    public static void updateBotStatus(int players) {
        String activity = players + " игроков | IP: " + serverIp + ":" + Config.port.num();
        jda.getPresence().setActivity(Activity.playing(activity));
    }

    public static void sendMessage(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(Strings.format(text, args)).build()).queue();
    }
}
