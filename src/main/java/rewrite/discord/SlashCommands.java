package rewrite.discord;

import arc.func.Cons;
import arc.struct.ObjectMap;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.jetbrains.annotations.NotNull;

import static rewrite.discord.Bot.botGuild;
import static rewrite.discord.Bot.jda;

public class SlashCommands extends ListenerAdapter {

    public static final ObjectMap<String, Cons<SlashCommandInteractionEvent>> commands = new ObjectMap<>();

    public static void load() {
        jda.addEventListener(new SlashCommands());

        registerCommand("reply", "Ответ на твое сообщение.", event -> event.reply("Ответ на твое сообщение.").queue()).queue();
        registerCommand("sum", "Сложить 2 числа.", event -> {
            long sum = event.getOption("first").getAsLong() + event.getOption("second").getAsLong();
            event.reply("Результат сложения: " + sum).queue();
        })
                .addOption(OptionType.INTEGER, "first", "Первое из двух чисел, которые надо сложить.", true)
                .addOption(OptionType.INTEGER, "second", "Второе из двух чисел, которые надо сложить.", true)
                .queue();
    }

    public static CommandCreateAction registerCommand(String name, String description, Cons<SlashCommandInteractionEvent> cons) {
        commands.put(name, cons);
        return botGuild.upsertCommand(name, description);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commands.get(event.getName()).get(event);
    }
}
