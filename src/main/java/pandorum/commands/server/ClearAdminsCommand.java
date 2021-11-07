package pandorum.commands.server;

import arc.util.Log;
import mindustry.gen.Groups;

import static mindustry.Vars.netServer;

public class ClearAdminsCommand implements ServerCommand {
    public static void run(final String[] args) {
        netServer.admins.getAdmins().each(admin -> netServer.admins.unAdminPlayer(admin.id));
        Groups.player.each(player -> player.admin(false));
        Log.info("Все админы сняты...");
    }
}
