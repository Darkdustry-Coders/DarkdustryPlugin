package pandorum.commands.server;

import arc.Core;
import arc.func.Cons;
import arc.util.Log;
import mindustry.net.Packets.KickReason;

import static mindustry.Vars.netServer;

public class RestartCommand implements Cons<String[]> {
    public void get(String[] args) {
        Log.info("Сервер перезапускается...");

        netServer.kickAll(KickReason.serverRestarting);
        Core.app.post(() -> System.exit(2));
    }
}
