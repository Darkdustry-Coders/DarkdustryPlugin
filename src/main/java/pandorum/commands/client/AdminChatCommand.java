package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class AdminChatCommand {
    @ClientCommand(name = "a", args = "<message...>", description = "Send message to admins.", admin = false)
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Groups.player.each(Player::admin, admin -> admin.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(admin.locale), Pal.adminChat, player.coloredName(), args[0]), player, args[0]));
    }
}
