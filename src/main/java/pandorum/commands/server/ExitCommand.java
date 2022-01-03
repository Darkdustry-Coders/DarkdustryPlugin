package pandorum.commands.server;

import arc.util.Log;

import static mindustry.Vars.net;

public class ExitCommand {
    public static void run(final String[] args) {
        Log.err("Выключаю сервер...");
        net.dispose();
        System.exit(2);
    }
}
