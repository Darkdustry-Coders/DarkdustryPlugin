package darkdustry.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;

import static arc.util.Strings.format;

public class SlashContext {

    public final SlashCommandInteractionEvent event;

    public final Member member;
    public final MessageChannel channel;

    public SlashContext(SlashCommandInteractionEvent event) {
        this.event = event;

        this.member = event.getMember();
        this.channel = event.getChannel();
    }

    public OptionMapping getOption(String name) {
        return event.getOption(name);
    }

    public void sendEmbed(MessageEmbed embed) {
        event.replyEmbeds(embed).queue();
    }

    public void success(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.green).build());
    }

    public void success(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.green).build());
    }

    public void info(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.yellow).build());
    }

    public void info(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.yellow).build());
    }

    public void error(String title, String text, Object... args) {
        sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.red).build());
    }

    public void error(String text, Object... args) {
        sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.red).build());
    }
}
