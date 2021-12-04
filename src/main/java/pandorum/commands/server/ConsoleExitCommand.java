package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.net;

public class ConsoleExitCommand {
    public static void run(final String[] args) {
        Log.info("Выключаю сервер.");
        net.dispose();
        System.exit(2);
    }
}
