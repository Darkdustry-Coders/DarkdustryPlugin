package pandorum.commands.server;

import arc.util.Log;
import mindustry.Vars;

public class ExitCommand {
    public static void run(final String[] args) {
        Log.info("Выключаю сервер.");
        Vars.net.dispose();
        System.exit(2);
    }
}
