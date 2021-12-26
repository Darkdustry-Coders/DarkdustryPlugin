package pandorum.commands.server;

import pandorum.Misc;
import pandorum.annotations.commands.ServerCommand;

public class ConsoleClearHistoryCommand {
    @ServerCommand(name = "clear-history", args = "", description = "clear history")
    public static void run(String[] args) {
        Misc.InitializeHistory();
    }
}
