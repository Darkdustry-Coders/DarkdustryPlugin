package pandorum.commands.client.admin;

import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.events.handlers.MenuHandler;

import static pandorum.Misc.findLocale;

public class UnitsDespawnCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        String[][] options = {{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.players", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.sharded", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.crux", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.suicide", findLocale(player.locale))}};
        Call.menu(player.con, MenuHandler.despwMenu, Bundle.format("commands.admin.despw.menu.header", findLocale(player.locale)), Bundle.format("commands.admin.despw.menu.content", findLocale(player.locale), Groups.unit.size()), options);
    }
}
