package pandorum.commands.server;

import arc.util.Log;
import pandorum.annotations.ServerCommand;

import static mindustry.Vars.net;

public class ConsoleExitCommand {
    @ServerCommand(name = "exit", args = "", description = "Shut down the server.")
    public static void run(final String[] args) {
        Log.info("Выключаю сервер.");
        net.dispose();
        System.exit(2);
    }
}
