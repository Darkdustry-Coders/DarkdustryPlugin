package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;

import static pandorum.Misc.findLocale;

public class ARTVCommand implements ClientCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        String[][] options = {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}};
        Call.menu(player.con, 2, Bundle.format("commands.admin.artv.menu.header", findLocale(player.locale)), Bundle.format("commands.admin.artv.menu.content", findLocale(player.locale)), options);
    }
}
