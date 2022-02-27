package pandorum.discord;

import arc.util.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Context {

    public final Message message;
    public final Member member;
    public final User author;
    public final MessageChannel channel;

    public final List<Attachment> attachments;
    public final String contentRaw;
    public final String contentDisplay;

    public Context(MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.member = event.getMember();
        this.author = event.getAuthor();
        this.channel = event.getChannel();

        this.attachments = message.getAttachments();
        this.contentRaw = message.getContentRaw();
        this.contentDisplay = message.getContentDisplay();
    }

    public void sendEmbed(MessageEmbed embed) {
        channel.sendMessageEmbeds(embed).queue();
    }

    public void success(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(Color.green).build());
    }

    public void info(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(Color.yellow).build());
    }

    public void err(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, Strings.format(text, args), true).setColor(Color.red).build());
    }

    public void success(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(Strings.format(text, args)).setColor(Color.green).build());
    }

    public void info(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(Strings.format(text, args)).setColor(Color.yellow).build());
    }

    public void err(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(Strings.format(text, args)).setColor(Color.red).build());
    }
}
