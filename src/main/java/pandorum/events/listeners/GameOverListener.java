package pandorum.events.listeners;

import mindustry.gen.Groups;
import pandorum.database.models.MapModel;
import pandorum.database.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call() {
        canVote = false;
        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();

        Groups.player.each(player -> PlayerModel.find(player, playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        MapModel.find(state.map, mapModel -> {
            mapModel.gamesPlayed++;
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
