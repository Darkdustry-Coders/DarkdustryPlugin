package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;

import static mindustry.Vars.state;
import static pandorum.listeners.handlers.MenuHandler.mapInfoMenu;
import static pandorum.util.Search.findLocale;

public class MapCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Call.menu(player.con, mapInfoMenu,
                Bundle.format("commands.map.menu.header", findLocale(player.locale), state.map.name()),
                Bundle.format("commands.map.menu.content", findLocale(player.locale), state.map.author(), state.map.description()),
                new String[][] {{Bundle.format("commands.map.menu.close", findLocale(player.locale))}}
        );
    }
}
