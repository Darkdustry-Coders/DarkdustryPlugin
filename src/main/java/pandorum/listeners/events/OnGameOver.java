package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.database.models.MapModel;
import pandorum.database.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class OnGameOver implements Cons<GameOverEvent>, Runnable {

    public void get(GameOverEvent event) {
        run();
    }

    public void run() {
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
