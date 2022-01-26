package pandorum.commands.server;

import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.Log;

import static pandorum.util.Utils.getServerControl;

public class HelpCommand {
    public static void run(final String[] args) {
        Seq<Command> commandsList = getServerControl().handler.getCommandList();
        Log.info("Команды для консоли: (@)", commandsList.size);
        commandsList.each(command -> Log.info("  &b&lb" + command.text + (command.paramText.isEmpty() ? "" : " &lc&fi") + command.paramText + "&fr - &lw" + command.description));
    }
}
