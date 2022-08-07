package rewrite.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rewrite.features.Authme;

import static arc.Core.*;
import static rewrite.PluginVars.*;
import static rewrite.discord.Bot.*;
import static rewrite.utils.Checks.*;

public class DiscordListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        MessageContext context = new MessageContext(event);
        app.post(() -> {
            handleMessage(context);
            sendMessageToGame(context);
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        switch (event.getComponentId()) {
            case "authme.confirm" -> Authme.confirm(event);
            case "authme.deny" -> Authme.deny(event);
            case "authme.info" -> Authme.info(event);
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (!loginWaiting.containsKey(event.getMessage()) || notAdmin(event)) return;

        if (event.getComponentId().equals("authme")) {
            switch (event.getValues().get(0)) {
                case "authme.confirm" -> Authme.confirm(event);
                case "authme.deny" -> Authme.deny(event);
                case "authme.info" -> Authme.info(event);
            }
        }
    }
}
