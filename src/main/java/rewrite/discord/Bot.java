package rewrite.discord;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.AllowedMentions;
import rewrite.DarkdustryPlugin;
import rewrite.utils.Find;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Locale;

import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.features.Authme.*;

public class Bot {

    public static JDA jda;

    public static Guild botGuild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken).addEventListeners(new DiscordListeners()).build();
            jda.awaitReady();

            botGuild = jda.getGuildById(config.discordGuildId);
            adminRole = botGuild.getRoleById(config.discordAdminRoleId);
            botChannel = botGuild.getTextChannelById(config.discordBotChannelId);
            adminChannel = botGuild.getTextChannelById(config.discordAdminChannelId);

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(MentionType.class));
            botGuild.getSelfMember().modifyNickname("[" + config.discordBotPrefix + "] " + jda.getSelfUser().getName()).queue();
            updateBotStatus();

            discordCommands = new CommandHandler(config.discordBotPrefix);
            DarkdustryPlugin.registerDiscordCommands(discordCommands);

            DarkdustryPlugin.info("Бот успешно подключен. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception exception) {
            DarkdustryPlugin.error("Не удалось запустить бота: @", exception);
        }
    }

    public static void handleMessage(MessageContext context) {
        CommandResponse response = discordCommands.handleMessage(context.message.getContentRaw(), context);
        if (response.type == ResponseType.noCommand || response.type == ResponseType.valid) return;

        if (response.type == ResponseType.unknownCommand)
            context.err(":interrobang: Неизвестная команда.", "Используй: **@help**, чтобы получить список доступных команд.", discordCommands.getPrefix());
        else if (response.type == ResponseType.fewArguments)
            context.err(":interrobang: Слишком мало аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
        else if (response.type == ResponseType.manyArguments)
            context.err(":interrobang: Слишком много аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
    }

    public static void sendMessageToGame(MessageContext context) {
        if (context.channel != botChannel || context.message.getContentDisplay().length() == 0) return;

        DarkdustryPlugin.discord("@: @", context.member.getEffectiveName(), context.message.getContentDisplay());

        var roles = context.member.getRoles();
        var reply = context.message.getReferencedMessage();

        Groups.player.each(player -> {
            Locale locale = Find.locale(player.locale);

            bundled(player, "discord.chat",
                    Integer.toHexString(context.member.getColorRaw()),
                    roles.isEmpty() ? format("discord.chat.no-role", locale) : roles.get(0).getName(),
                    context.member.getEffectiveName(),
                    reply != null ? format("discord.chat.reply", locale, botGuild.retrieveMember(reply.getAuthor()).complete().getEffectiveName()) : "",
                    context.message.getContentDisplay());
        });
    }

    public static void sendAdminRequest(Player player) {
        adminChannel.sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle("Запрос на получение прав администратора.")
                .addField("Никнейм:", player.name, true)
                .addField("UUID:", player.uuid(), true)
                .setFooter("Нажмите на кнопку, чтобы подтвердить или отклонить запрос. Подтверждайте только свои запросы!").build()
        ).setActionRows(ActionRow.of(confirm, deny, info)).build()).queue(message -> loginWaiting.put(message, player.uuid()));
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(Permission.ADMINISTRATOR));
    }

    public static void updateBotStatus() {
        jda.getPresence().setActivity(Activity.playing(Groups.player.size() + " игроков | IP: " + config.hubIp + ":" + Config.port.num()));
    }

    public static void sendMessage(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(Strings.format(text, args)).build()).queue();
    }
}
