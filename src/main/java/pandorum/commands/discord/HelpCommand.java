package pandorum.commands.discord;

import net.dv8tion.jda.api.entities.Message;

import static pandorum.discord.Bot.discordHandler;
import static pandorum.discord.Bot.info;

public class HelpCommand {
    public static void run(final String[] args, final Message message) {
        StringBuilder commands = new StringBuilder();
        discordHandler.getCommandList().each(command -> {
            commands.append(discordHandler.getPrefix()).append("**").append(command.text).append("**");
            if (command.params.length > 0) {
                commands.append(" *").append(command.paramText).append("*");
            }
            commands.append(" - ").append(command.description).append("\n");
        });

        info(message.getChannel(), "Команды:", commands.toString());
    }
}
