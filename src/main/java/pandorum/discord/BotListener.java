package pandorum.discord;

import arc.util.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pandorum.features.Authme;

import java.awt.*;

import static pandorum.discord.Bot.*;
import static pandorum.util.Utils.adminCheck;
import static pandorum.util.Utils.sendToChat;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Context context = new Context(event);

        if (context.author.getIdLong() == jda.getSelfUser().getIdLong() || !context.message.isFromGuild()) return;

        handleMessage(context);

        if (context.channel.getIdLong() == botChannel.getIdLong() && context.contentDisplay.length() > 0) {
            sendToChat("events.discord.chat", context.member.getEffectiveName(), context.author.getDiscriminator(), context.contentDisplay);
            Log.info("[Discord] @#@: @", context.member.getEffectiveName(), context.author.getDiscriminator(), context.contentDisplay);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();

        if (!adminCheck(member)) {
            event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setTitle(":no_entry_sign: Взаимодействовать с запросами могут только админы.").build()).setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "admin.confirm" -> Authme.confirm(message, member);
            case "admin.deny" -> Authme.deny(message, member);
            case "admin.ban" -> Authme.ban(message, member);
            case "admin.info" -> Authme.info(message, event);
        }
    }
}
