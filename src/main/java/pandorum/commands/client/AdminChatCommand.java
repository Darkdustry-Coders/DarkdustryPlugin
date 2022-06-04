package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import pandorum.components.Bundle;
import pandorum.util.Utils;

import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.bundled;

public class AdminChatCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!Utils.isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        Groups.player.each(Player::admin, p -> p.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(p.locale), Pal.adminChat, player.coloredName(), args[0]), player, args[0]));
    }
}
