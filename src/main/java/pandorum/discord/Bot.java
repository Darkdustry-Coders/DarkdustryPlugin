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
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.AllowedMentions;
import pandorum.Loader;

import java.awt.*;
import java.util.EnumSet;

import static mindustry.Vars.netServer;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.discordCommands;

public class Bot {

    public static JDA jda;

    public static Guild guild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createDefault(config.discordBotToken).build().awaitReady();
            jda.addEventListener(new BotListener());

            guild = jda.getGuildById(config.discordGuildID);
            adminRole = guild.getRoleById(config.discordAdminRoleID);
            botChannel = guild.getTextChannelById(config.discordBotChannelID);
            adminChannel = guild.getTextChannelById(config.discordAdminChannelID);

            AllowedMentions.setDefaultMentions(EnumSet.noneOf(MentionType.class));

            guild.getSelfMember().modifyNickname("[" + config.discordBotPrefix + "] " + jda.getSelfUser().getName()).queue();

            discordCommands = new CommandHandler(config.discordBotPrefix);
            Loader.registerDiscordCommands(discordCommands);

            Log.info("[Darkdustry] Бот успешно подключен... (@)", jda.getSelfUser().getAsTag());
        } catch (Exception e) {
            Log.err("[Darkdustry] Не удалось запустить бота...");
            Log.err(e);
        }
    }

    public static void handleMessage(Context context) {
        CommandResponse response = discordCommands.handleMessage(context.contentRaw, context);
        if (response.type == ResponseType.fewArguments) {
            context.err(":interrobang: Слишком мало аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
        } else if (response.type == ResponseType.manyArguments) {
            context.err(":interrobang: Слишком много аргументов.", "Использование: **@@** @", discordCommands.getPrefix(), response.command.text, response.command.paramText);
        }
    }

    public static void updateBotStatus() {
        Activity activity = Activity.playing(netServer.admins.getPlayerLimit() > 0 ? Strings.format("@ / @ игроков онлайн", Groups.player.size(), netServer.admins.getPlayerLimit()) : Strings.format("@ игроков онлайн", Groups.player.size()));
        jda.getPresence().setActivity(activity);
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
