package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static pandorum.listeners.handlers.MenuHandler.artvMenu;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findLocale;

public class ArtvCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        Call.menu(player.con, artvMenu,
                Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)),
                new String[][] {{Bundle.format("ui.menus.yes", findLocale(player.locale)), Bundle.format("ui.menus.no", findLocale(player.locale))}}
        );
    }
}
