package pandorum.events.listeners;

import mindustry.gen.Groups;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call() {
        canVote = false;
        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();

        Groups.player.each(player -> playersInfo.find(player, playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        mapsInfo.find(state.map, mapModel -> {
            mapModel.gamesPlayed++;
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
