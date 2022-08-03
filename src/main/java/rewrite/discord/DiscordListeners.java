package rewrite.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;

import static arc.Core.*;
import static rewrite.PluginVars.*;
import static rewrite.discord.Bot.*;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        MessageContext context = new MessageContext(event);
        app.post(() -> {
            handleMessage(context);
            sendMessageToGame(context);
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage())) return;

        if (!isAdmin(event.getMember())) {
            event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
            return;
        }

        // switch (event.getComponentId()) { // TODO: добавить authme
        //     case "authme.confirm" -> Authme.confirm(event);
        //     case "authme.deny" -> Authme.deny(event);
        //     case "authme.info" -> Authme.info(event);
        // }
    }
}
