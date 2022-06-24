package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.features.Authme;

import java.awt.*;

import static pandorum.PluginVars.loginWaiting;
import static pandorum.discord.Bot.*;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.PlayerUtils.sendToChat;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Context context = new Context(event);

        if (!context.message.isFromGuild() || context.message.getGuild() != botGuild || context.member == null || context.member == botGuild.getSelfMember())
            return;

        handleMessage(context);

        if (context.channel == botChannel) {
            sendToChat("events.discord.chat", context.member.getEffectiveName(), context.message.getContentDisplay());
            Log.info("[Discord] @: @", context.member.getEffectiveName(), context.message.getContentDisplay());
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Context context = new Context(event);

        if (loginWaiting.containsKey(context.message)) {
            if (!isAdmin(context.member)) {
                event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
                return;
            }

            switch (event.getComponentId()) {
                case "admin.confirm" -> Authme.confirm(context);
                case "admin.deny" -> Authme.deny(context);
                case "admin.info" -> Authme.info(context, event);
            }
        }
    }
}
