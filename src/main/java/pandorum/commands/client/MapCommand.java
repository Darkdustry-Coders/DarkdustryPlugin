package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.database.models.MapModel;

import static mindustry.Vars.state;
import static pandorum.events.handlers.MenuHandler.mapRateMenu;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.secondsToMinutes;

public class MapCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        MapModel.find(state.map, mapModel -> Call.menu(player.con,
                mapRateMenu,
                Bundle.format("commands.map.menu.header", findLocale(player.locale), state.map.name()),
                Bundle.format("commands.map.menu.content", findLocale(player.locale), state.map.author(), state.map.description(), mapModel.upVotes, mapModel.downVotes, secondsToMinutes(mapModel.playTime), mapModel.gamesPlayed, mapModel.bestWave),
                new String[][] {{Bundle.format("commands.map.menu.upvote", findLocale(player.locale)), Bundle.format("commands.map.menu.downvote", findLocale(player.locale))}, {Bundle.format("commands.map.menu.close", findLocale(player.locale))}}
        ));
    }
}
