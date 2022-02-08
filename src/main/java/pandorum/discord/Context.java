package pandorum.discord;

import arc.files.Fi;
import arc.util.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Context {

    public Message message;
    public Member member;
    public User author;
    public MessageChannel channel;

    public List<Attachment> attachments;
    public String content;

    public Context(MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.member = event.getMember();
        this.author = event.getAuthor();
        this.channel = event.getChannel();

        this.attachments = message.getAttachments();
        this.content = message.getContentRaw();
    }

    public void sendEmbed(MessageEmbed embed) {
        channel.sendMessageEmbeds(embed).queue();
    }

    public void sendEmbedWithFile(MessageEmbed embed, Fi file) {
        channel.sendMessageEmbeds(embed).addFile(file.file()).queue();
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
}
