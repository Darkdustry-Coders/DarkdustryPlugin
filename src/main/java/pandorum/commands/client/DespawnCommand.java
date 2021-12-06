package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;
import pandorum.events.handlers.MenuHandler;

import static mindustry.Vars.state;
import static pandorum.Misc.colorizedTeam;
import static pandorum.Misc.findLocale;

public class DespawnCommand {
    public static void run(final String[] args, final Player player) {
        if (Misc.adminCheck(player)) return;
        Call.menu(player.con,
                MenuHandler.despwMenu,
                Bundle.format("commands.admin.despw.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.despw.menu.content", findLocale(player.locale), Groups.unit.size()),
                new String[][]{{Bundle.format("events.menu.yes", findLocale(player.locale)), Bundle.format("events.menu.no", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.players", findLocale(player.locale))}, {Bundle.format("commands.admin.despw.menu.team", findLocale(player.locale), colorizedTeam(state.rules.defaultTeam))}, {Bundle.format("commands.admin.despw.menu.team", findLocale(player.locale), colorizedTeam(state.rules.waveTeam))}, {Bundle.format("commands.admin.despw.menu.suicide", findLocale(player.locale))}}
        );
    }
}
