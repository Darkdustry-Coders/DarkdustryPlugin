package rewrite.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static arc.Core.*;
import static rewrite.PluginVars.*;
import static rewrite.utils.Checks.*;
import static rewrite.discord.Bot.*;

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
        if (!loginWaiting.containsKey(event.getMessage())) return;
        if (notAdmin(event)) return;

        // switch (event.getComponentId()) { // TODO: добавить authme
        //     case "authme.confirm" -> Authme.confirm(event);
        //     case "authme.deny" -> Authme.deny(event);
        //     case "authme.info" -> Authme.info(event);
        // }
    }
}
