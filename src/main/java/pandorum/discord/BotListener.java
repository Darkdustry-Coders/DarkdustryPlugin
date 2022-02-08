package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.components.Authme;

import static pandorum.discord.Bot.*;
import static pandorum.util.Utils.sendToChat;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Context context = new Context(event);

        if (context.author.getIdLong() == jda.getSelfUser().getIdLong() || context.channel.getType() == ChannelType.PRIVATE)
            return;

        handleMessage(context);

        if (context.channel.getIdLong() == botChannel.getIdLong() && context.content.length() > 0) {
            sendToChat("events.discord.chat", context.author.getAsTag(), context.content);
            Log.info("[Discord] @: @", context.author.getAsTag(), context.content);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Message message = event.getMessage();
        User user = event.getUser();
        switch (event.getComponentId()) {
            case "admin.confirm" -> Authme.confirm(message, user);
            case "admin.deny" -> Authme.deny(message, user);
            case "admin.ban" -> Authme.ban(message, user);
            case "admin.check" -> Authme.check(message, event);
        }
    }
}
