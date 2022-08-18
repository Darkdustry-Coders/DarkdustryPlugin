package darkdustry.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.awt.Color;

import static arc.util.Strings.format;

public record SlashContext(SlashCommandInteractionEvent event) {

    public OptionMapping getOption(String name) {
        return event.getOption(name);
    }

    public ReplyCallbackAction sendEmbed(MessageEmbed embed) {
        return event.replyEmbeds(embed);
    }

    public ReplyCallbackAction success(String title, String text, Object... args) {
        return sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.green).build());
    }

    public ReplyCallbackAction success(String text, Object... args) {
        return sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.green).build());
    }

    public ReplyCallbackAction info(String title, String text, Object... args) {
        return sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.yellow).build());
    }

    public ReplyCallbackAction info(String text, Object... args) {
        return sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.yellow).build());
    }

    public ReplyCallbackAction error(String title, String text, Object... args) {
        return sendEmbed(new EmbedBuilder().addField(title, format(text, args), true).setColor(Color.red).build());
    }

    public ReplyCallbackAction error(String text, Object... args) {
        return sendEmbed(new EmbedBuilder().setTitle(format(text, args)).setColor(Color.red).build());
    }
}
