package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.events.handlers.MenuHandler;

import static pandorum.util.Search.findLocale;

public class ArtvCommand {
    public static void run(final String[] args, final Player player) {
        Call.menu(player.con,
                MenuHandler.artvMenu,
                Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)),
                new String[][] {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}}
        );
    }
}
