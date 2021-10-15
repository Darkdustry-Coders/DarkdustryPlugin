package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;

import static pandorum.Misc.bundled;

public class AdminChatCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Groups.player.each(Player::admin, admin -> bundled(admin, "commands.admin.a.chat", Misc.colorizedName(player), args[0]));
    }
}
