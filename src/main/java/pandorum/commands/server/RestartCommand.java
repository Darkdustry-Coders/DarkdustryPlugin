package pandorum.commands.server;

import arc.util.Log;
import arc.util.Time;
import mindustry.net.Packets.KickReason;

import static mindustry.Vars.netServer;

public class RestartCommand {
    public static void run(final String[] args) {
        Log.info("Сервер перезапускается...");

        netServer.kickAll(KickReason.serverRestarting);
        Time.runTask(60f, () -> System.exit(2));
    }
}
