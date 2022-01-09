package pandorum.commands.server;

import arc.util.Log;

public class ExitCommand {
    public static void run(final String[] args) {
        Log.info("Выключаю сервер...");
        System.exit(2);
    }
}
