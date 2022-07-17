package pandorum.discord;

import arc.Core;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static pandorum.discord.Bot.handleMessage;
import static pandorum.discord.Bot.sendMessageToGame;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        MessageContext context = new MessageContext(event);
        Core.app.post(() -> {
            handleMessage(context);
            sendMessageToGame(context);
        });
    }
}
