package pandorum.events.listeners;

import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.models.MapModel;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;
import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call(final GameOverEvent event) {
        call();
    }

    public static void call() {
        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        mapRateVotes.clear();
        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();

        Groups.player.each(player -> PlayerModel.find(player.uuid(), playerInfo -> {
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
