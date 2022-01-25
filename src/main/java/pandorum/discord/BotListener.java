package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.comp.Authme;

import static pandorum.Misc.sendToChat;
import static pandorum.discord.Bot.*;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = message.getMember();

        if (member.getUser().getIdLong() == jda.getSelfUser().getIdLong() || event.isFromType(ChannelType.PRIVATE)) return;

        handleMessage(message);

        if (message.getChannel().getIdLong() == botChannel.getIdLong() && !message.getContentRaw().isBlank()) {
            sendToChat("events.discord.chat", member.getNickname(), message.getContentRaw());
            Log.info("[Discord] @: @", member.getNickname(), message.getContentRaw());
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
