package pandorum.commands.client.admin;

import mindustry.gen.Player;
import pandorum.Misc;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class BanCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        if (netServer.admins.getInfoOptional(args[0]) != null && netServer.admins.banPlayerID(args[0]))
            bundled(player, "commands.admin.ban.success", netServer.admins.getInfo(args[0]).lastName);
        else
            bundled(player, "commands.admin.ban.fail");
    }
}