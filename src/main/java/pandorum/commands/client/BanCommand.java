package pandorum.commands.client;

import mindustry.gen.Player;
import mindustry.net.Administration.PlayerInfo;

import static mindustry.Vars.netServer;
import static pandorum.Misc.bundled;

public class BanCommand {
    public static void run(final String[] args, final Player player) {
        PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
        if (info != null && netServer.admins.banPlayerID(args[0])) {
            bundled(player, "commands.admin.ban.success", info.lastName);
            return;
        }

        bundled(player, "commands.admin.ban.fail");
    }
}