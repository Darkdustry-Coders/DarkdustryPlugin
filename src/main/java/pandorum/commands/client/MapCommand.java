package pandorum.commands.client;

import mindustry.gen.Call;
import mindustry.gen.Player;
import pandorum.comp.Bundle;
import pandorum.models.MapModel;

import static mindustry.Vars.state;
import static pandorum.events.handlers.MenuHandler.mapRateMenu;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.millisecondsToMinutes;

public class MapCommand {
    public static void run(final String[] args, final Player player) {
        MapModel.find(state.map, mapModel -> Call.menu(player.con,
                mapRateMenu,
                Bundle.format("commands.map.header", findLocale(player.locale), state.map.name()),
                Bundle.format("commands.map.content", findLocale(player.locale), state.map.author(), state.map.description(), mapModel.upVotes, mapModel.downVotes, millisecondsToMinutes(mapModel.playTime), mapModel.gamesPlayed, mapModel.bestWave),
                new String[][] {{Bundle.format("commands.map.upvote", findLocale(player.locale)), Bundle.format("commands.map.downvote", findLocale(player.locale))}, {Bundle.format("commands.map.close", findLocale(player.locale))}}
        ));
    }
}
