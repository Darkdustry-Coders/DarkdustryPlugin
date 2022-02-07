package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.events.handlers.MenuHandler;

import static pandorum.util.Search.findLocale;

public class ArtvCommand {
    public static void run(final String[] args, final Player player) {
        Call.menu(player.con,
                MenuHandler.artvMenu,
                Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)),
                new String[][] {{Bundle.format("ui.menus.yes", findLocale(player.locale)), Bundle.format("ui.menus.no", findLocale(player.locale))}}
        );
    }
}
