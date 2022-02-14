package pandorum.discord;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import pandorum.commands.DiscordCommandsLoader;

import java.awt.*;
import java.util.EnumSet;

import static pandorum.PluginVars.config;

public class Bot {

    public static final CommandHandler discordHandler = new CommandHandler(config.discordBotPrefix);

    public static JDA jda;

    public static Guild guild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void init() {
        try {
            jda = JDABuilder.createDefault(config.discordBotToken).build().awaitReady();
            jda.addEventListener(new BotListener());

            guild = jda.getGuildById(config.discordGuildID);
            adminRole = guild.getRoleById(config.discordAdminRoleID);
            botChannel = guild.getTextChannelById(config.discordBotChannelID);
            adminChannel = guild.getTextChannelById(config.discordAdminChannelID);

            DiscordCommandsLoader.registerDiscordCommands(discordHandler);
            AllowedMentions.setDefaultMentions(EnumSet.noneOf(MentionType.class));

            Log.info("[Darkdustry] Бот успешно запущен...");
        } catch (Exception e) {
            Log.err("[Darkdustry] Ошибка запуска бота...");
            Log.err(e);
        }
    }

    public static void handleMessage(Context context) {
        CommandResponse response = discordHandler.handleMessage(context.contentRaw, context);
        if (response.type == ResponseType.fewArguments) {
            context.err(":interrobang: Слишком мало аргументов.", "Использование: **@@** @", discordHandler.getPrefix(), response.command.text, response.command.paramText);
        } else if (response.type == ResponseType.manyArguments) {
            context.err(":interrobang: Слишком много аргументов.", "Использование: **@@** @", discordHandler.getPrefix(), response.command.text, response.command.paramText);
        }
    }

    public static boolean adminCheck(Member member) {
        return member.getRoles().contains(adminRole);
    }

    public static void updateBotStatus() {
        jda.getPresence().setActivity(EntityBuilder.createActivity(Strings.format("@ игроков онлайн", Groups.player.size()), null, ActivityType.STREAMING));
    }

    public static void text(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public static void text(String text, Object... args) {
        text(botChannel, text, args);
    }

    public static void sendEmbed(MessageChannel channel, Color color, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(Strings.format(text, args)).build()).queue();
    }

    public static void sendEmbed(Color color, String text, Object... args) {
        sendEmbed(botChannel, color, text, args);
    }
}
