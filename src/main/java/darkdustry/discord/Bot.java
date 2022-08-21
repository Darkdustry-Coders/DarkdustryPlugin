package darkdustry.discord;

import arc.util.Strings;
import darkdustry.commands.DiscordCommands;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration.Config;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Find;

import java.awt.Color;
import java.util.EnumSet;

import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.*;
import static darkdustry.features.Authme.*;

public class Bot {

    public static JDA jda;

    public static Guild botGuild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListeners()).build().awaitReady();

            botGuild = jda.getGuildById(config.discordGuildId);
            adminRole = botGuild.getRoleById(config.discordAdminRoleId);
            botChannel = botGuild.getTextChannelById(config.discordBotChannelId);
            adminChannel = botGuild.getTextChannelById(config.discordAdminChannelId);

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(MentionType.class));
            botGuild.updateCommands().queue();

            DiscordCommands.load();

            updateBotStatus();

            DarkdustryPlugin.info("Bot connected. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception exception) {
            DarkdustryPlugin.error("Failed to connect bot: @", exception);
        }
    }

    public static void exit() {
        jda.shutdownNow();
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
        adminChannel.sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle("Запрос на получение прав администратора.")
                .addField("Никнейм:", player.name, true)
                .addField("UUID:", player.uuid(), true)
                .setFooter("Выберите нужную опцию, чтобы подтвердить или отклонить запрос. Подтверждайте только свои запросы!").build()
        ).setActionRows(ActionRow.of(menu)).build()).queue(message -> loginWaiting.put(message, player.uuid()));
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(Permission.ADMINISTRATOR));
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
