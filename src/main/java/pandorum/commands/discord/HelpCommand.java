package pandorum.commands.discord;

import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import pandorum.discord.MessageContext;

import java.util.Comparator;

import static pandorum.PluginVars.discordCommands;

public class HelpCommand implements CommandRunner<MessageContext> {
    public void accept(String[] args, MessageContext context) {
        Seq<Command> commandsList = discordCommands.getCommandList().sort(Comparator.comparing(command -> command.text));
        StringBuilder commands = new StringBuilder();

        for (Command command : commandsList) {
            commands.append(discordCommands.getPrefix()).append("**").append(command.text).append("**");
            if (!command.paramText.isEmpty()) {
                commands.append(" *").append(command.paramText).append("*");
            }
            commands.append(" - ").append(command.description).append("\n");
        }

        context.info(":newspaper: Доступные команды:", commands.toString());
    }
}
