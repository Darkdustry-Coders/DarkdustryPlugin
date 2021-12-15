package pandorum.commands.server;

import arc.util.Log;
import mindustry.net.Administration.PlayerInfo;
import pandorum.annotations.commands.OverrideCommand;
import pandorum.annotations.commands.ServerCommand;

import static mindustry.Vars.netServer;

public class ConsolePardonCommand {
    @OverrideCommand
    @ServerCommand(name = "pardon", args = "<uuid...>", description = "Pardon a kicked player.")
    public static void run(final String[] args) {
        PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
        if (info != null) {
            info.lastKicked = 0;
            netServer.admins.kickedIPs.remove(info.lastIP);
            Log.info("Игрок @ снова может зайти на сервер.", info.lastName);
        } else {
            Log.err("Игрок не найден...");
        }
    }
}
