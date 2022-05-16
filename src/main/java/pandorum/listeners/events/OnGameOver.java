package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.data.PlayerData;
import pandorum.mongo.models.MapModel;

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

        Groups.player.each(player -> {
            PlayerData data = datas.get(player.uuid());
            data.gamesPlayed++;
        });

        MapModel.find(state.map, mapModel -> {
            mapModel.gamesPlayed++;
            mapModel.bestWave = Math.max(mapModel.bestWave, state.wave);
            mapModel.save();
        });
    }
}
