package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import pandorum.components.Bundle;

import static pandorum.util.Search.findLocale;

public class AdminChatCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Groups.player.each(Player::admin, p -> p.sendMessage(Bundle.format("commands.admin.a.chat", findLocale(p.locale), Pal.adminChat, player.coloredName(), args[0]), player, args[0]));
    }
}
