package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.commands.DiscordCommandsLoader;
import pandorum.comp.Authme;

import static pandorum.Misc.sendToChat;
import static pandorum.PluginVars.config;
import static pandorum.discord.Bot.*;

public class BotListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        guild = jda.getGuildById(config.discordGuildID);
        adminRole = guild.getRoleById(config.discordAdminRoleID);
        botChannel = guild.getTextChannelById(config.discordBotChannelID);
        adminChannel = guild.getTextChannelById(config.discordAdminChannelID);

        DiscordCommandsLoader.registerDiscordCommands(discordHandler);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = message.getMember();

        if (member.getUser().getIdLong() == jda.getSelfUser().getIdLong()) return;

        handleMessage(message);

        if (message.getChannel().getIdLong() == botChannel.getIdLong() && !message.getContentRaw().isBlank()) {
            sendToChat("events.discord.chat", member.getAsMention(), message.getContentRaw());
            Log.info("[Discord] @: @", member.getAsMention(), message.getContentRaw());
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();
        switch (event.getComponentId()) {
            case "admin.confirm" -> Authme.confirm(message, member);
            case "admin.deny" -> Authme.deny(message, member);
            case "admin.check" -> Authme.check(message, event);
        }
    }
}
