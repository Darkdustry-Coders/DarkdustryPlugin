package pandorum.comp;

import mindustry.gen.Player;
import pandorum.Misc;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class Authme {
    public static void addAdmin(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player == null) return;
        netServer.admins.adminPlayer(player.uuid(), player.usid());
        player.admin(true);
        bundled(player, "commands.login.success");
    }

    public static void ignoreRequest(String uuid) {
        Player player = Misc.findByID(uuid);
        if (player == null) return;
        bundled(player, "commands.login.ignore");
    }
}
