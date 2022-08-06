package rewrite.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rewrite.features.Authme;

import static arc.Core.app;
import static rewrite.PluginVars.loginWaiting;
import static rewrite.discord.Bot.handleMessage;
import static rewrite.discord.Bot.sendMessageToGame;
import static rewrite.utils.Checks.notAdmin;

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
}
