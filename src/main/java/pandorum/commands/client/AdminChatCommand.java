package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Playerc;
import mindustry.graphics.Pal;
import pandorum.Misc;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class AdminChatCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Groups.player.each(Playerc::admin, admin -> admin.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(admin.locale), Pal.adminChat.toString(), player.coloredName(), args[0]), player, args[0]));
    }
}
