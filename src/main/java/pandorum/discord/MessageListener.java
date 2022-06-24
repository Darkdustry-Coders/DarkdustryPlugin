package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static pandorum.discord.Bot.botChannel;
import static pandorum.discord.Bot.handleMessage;
import static pandorum.util.PlayerUtils.sendToChat;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        handleMessage(new Context(event));

        if (event.getChannel() == botChannel) {
            sendToChat("events.discord.chat", Integer.toHexString(event.getMember().getColorRaw()), event.getAuthor().getAsTag(), event.getMessage().getContentDisplay());
            Log.info("[Discord] @: @", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay());
        }
    }
}
