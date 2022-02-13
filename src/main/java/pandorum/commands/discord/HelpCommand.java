package pandorum.commands.discord;

import pandorum.discord.Context;

import static pandorum.discord.Bot.discordHandler;

public class HelpCommand {
    public static void run(final String[] args, final Context context) {
        StringBuilder commands = new StringBuilder();
        discordHandler.getCommandList().each(command -> {
            commands.append(discordHandler.getPrefix()).append("**").append(command.text).append("**");
            if (!command.paramText.isEmpty()) {
                commands.append(" *").append(command.paramText).append("*");
            }
            commands.append(" - ").append(command.description).append("\n");
        });

        context.info(":newspaper: All available commands:", commands.toString());
    }
}
