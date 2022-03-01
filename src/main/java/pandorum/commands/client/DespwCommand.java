package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.events.handlers.MenuHandler;

import static mindustry.Vars.state;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.coloredTeam;

public class DespwCommand {
    public static void run(final String[] args, final Player player) {
        Call.menu(player.con,
                MenuHandler.despwMenu,
                Bundle.format("commands.admin.despw.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.despw.menu.content", findLocale(player.locale), Groups.unit.size()),
                new String[][] {{Bundle.format("ui.menus.yes", findLocale(player.locale)), Bundle.format("ui.menus.no", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.players", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.team", findLocale(player.locale), coloredTeam(state.rules.defaultTeam))}, {Bundle.format("commands.admin.despw.menu.team", findLocale(player.locale), coloredTeam(state.rules.waveTeam))}, {Bundle.format("commands.admin.despw.menu.suicide", findLocale(player.locale))}}
        );
    }
}
