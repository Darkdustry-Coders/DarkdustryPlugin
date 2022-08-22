package darkdustry.discord;

import arc.util.Strings;
import darkdustry.commands.DiscordCommands;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.awt.Color;
import java.util.EnumSet;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.features.Authme.*;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.api.entities.Message.MentionType.*;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;
import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class Bot {

    public static JDA jda;

    public static Guild botGuild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListeners()).build().awaitReady();

            botGuild = jda.getGuildById(config.discordGuildId);
            adminRole = botGuild.getRoleById(config.discordAdminRoleId);
            botChannel = botGuild.getTextChannelById(config.discordBotChannelId);
            adminChannel = botGuild.getTextChannelById(config.discordAdminChannelId);

            MessageRequest.setDefaultMentions(EnumSet.of(CHANNEL, EMOJI));
            botGuild.updateCommands().queue();

            DiscordCommands.load();

            updateBotStatus();

            DarkdustryPlugin.info("Bot connected. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception exception) {
            DarkdustryPlugin.error("Failed to connect bot: @", exception);
        }
    }

    public static void exit() {
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
                    reply != null ? format("discord.chat.reply", locale, botGuild.retrieveMember(reply.getAuthor()).complete().getEffectiveName()) : "",
                    message.getContentDisplay());
        });
    }

    public static void sendAdminRequest(Player player) {
        adminChannel.sendMessage(new MessageCreateBuilder().setEmbeds(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle("Запрос на получение прав администратора.")
                .addField("Никнейм:", player.name, true)
                .addField("UUID:", player.uuid(), true)
                .setFooter("Выберите нужную опцию, чтобы подтвердить или отклонить запрос. Подтверждайте только свои запросы!").build()
        ).setComponents(of(menu)).build()).queue(message -> loginWaiting.put(message, player.uuid()));
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(ADMINISTRATOR));
    }

    public static void updateBotStatus() {
        jda.getPresence().setActivity(Activity.playing(Groups.player.size() + " игроков на " + serverIp + ":" + Config.port.num()));
    }

    public static void sendMessage(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(Strings.format(text, args)).build()).queue();
    }
}
