package pandorum.commands.discord;

import pandorum.discord.Context;
import pandorum.util.Utils;

import static pandorum.discord.Bot.discordHandler;
import static pandorum.util.Utils.adminCheck;

public class HelpCommand {
    public static void run(final String[] args, final Context context) {
        StringBuilder commands = new StringBuilder();
        Utils.getAvailableDiscordCommands(adminCheck(context.member)).each(command -> {
            commands.append(discordHandler.getPrefix()).append("**").append(command.text).append("**");
            if (!command.paramText.isEmpty()) {
                commands.append(" *").append(command.paramText).append("*");
            }
            commands.append(" - ").append(command.description).append("\n");
        });

        context.info(":newspaper: Доступные команды:", commands.toString());
    }
}
