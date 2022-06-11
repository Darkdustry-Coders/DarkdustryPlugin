package pandorum.discord;

import arc.util.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class Context {

    public final Message message;
    public final Member member;
    public final MessageChannel channel;

    public Context(MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.member = event.getMember();
        this.channel = event.getChannel();
    }

    public Context(ButtonInteractionEvent event) {
        this.message = event.getMessage();
        this.member = event.getMember();
        this.channel = event.getChannel();
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
