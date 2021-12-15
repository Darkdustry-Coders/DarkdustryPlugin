package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.annotations.commands.ClientCommand;
import pandorum.annotations.commands.admin.RequireAdmin;
import pandorum.annotations.commands.gamemodes.RequireSimpleGamemode;
import pandorum.comp.Bundle;
import pandorum.events.handlers.MenuHandler;

import static pandorum.Misc.findLocale;

public class ARTVCommand {
    @RequireSimpleGamemode
    @RequireAdmin
    @ClientCommand(name = "artv", args = "", description = "Force a gameover.")
    public static void run(final String[] args, final Player player) {
        Call.menu(player.con,
                MenuHandler.artvMenu,
                Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)),
                new String[][] {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}}
        );
    }
}
