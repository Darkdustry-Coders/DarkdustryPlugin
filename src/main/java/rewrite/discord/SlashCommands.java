package rewrite.discord;

import arc.util.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import static rewrite.discord.Bot.jda;

public class SlashCommands extends ListenerAdapter {

    public static void load() {
        jda.upsertCommand("test", "Test")
                .addOption(OptionType.INTEGER, "test", "Test", true)
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("test")) {
            Log.info(event.getOption("test").getAsInt());
        }
    }
}
