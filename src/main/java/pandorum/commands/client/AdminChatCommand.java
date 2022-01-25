package pandorum.commands.client;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import pandorum.comp.Bundle;

import static pandorum.utils.Search.findLocale;

public class AdminChatCommand {
    public static void run(final String[] args, final Player player) {
        Groups.player.each(Player::admin, p -> p.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(p.locale), Pal.adminChat, player.coloredName(), args[0]), player, args[0]));
    }
}
