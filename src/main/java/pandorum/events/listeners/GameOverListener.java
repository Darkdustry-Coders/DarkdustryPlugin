package pandorum.events.listeners;

import mindustry.gen.Groups;
import pandorum.database.databridges.MapInfo;
import pandorum.database.databridges.PlayerInfo;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call() {
        canVote = false;
        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();

        Groups.player.each(player -> PlayerInfo.find(player, playerModel -> {
            playerModel.gamesPlayed++;
            PlayerInfo.save(playerModel);
        }));

        MapInfo.find(state.map, mapModel -> {
            mapModel.gamesPlayed++;
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            MapInfo.save(mapModel);
        });
    }
}
