package pandorum.commands.server;

import arc.util.Log;
import arc.util.Timer;
import mindustry.gen.Groups;
import pandorum.Misc;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Перезапуск сервера...");

        Groups.player.each(Misc::connectToHub);
        Timer.schedule(() -> System.exit(2), 5f);
    }
}
