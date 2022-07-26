package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static mindustry.Vars.state;
import static pandorum.listeners.handlers.MenuHandler.despawnMenu;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.PlayerUtils.isAdmin;
import static pandorum.util.Search.findLocale;
import static pandorum.util.StringUtils.coloredTeam;

public class DespawnCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        if (!isAdmin(player)) {
            bundled(player, "commands.permission-denied");
            return;
        }

        Call.menu(player.con, despawnMenu,
                Bundle.format("commands.despawn.menu.header", findLocale(player.locale)),
                Bundle.format("commands.despawn.menu.content", findLocale(player.locale), Groups.unit.size()),
                new String[][] {{Bundle.format("ui.menus.yes", findLocale(player.locale)), Bundle.format("ui.menus.no", findLocale(player.locale))}, {Bundle.format("commands.despawn.menu.players", findLocale(player.locale))}, {Bundle.format("commands.despawn.menu.team", findLocale(player.locale), coloredTeam(state.rules.defaultTeam))}, {Bundle.format("commands.despawn.menu.team", findLocale(player.locale), coloredTeam(state.rules.waveTeam))}, {Bundle.format("commands.despawn.menu.suicide", findLocale(player.locale))}}
        );
    }
}
