package pandorum.events.listeners;

import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.models.PlayerModel;

import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call(final GameOverEvent event) {
        Groups.player.each(player -> PlayerModel.find(player.uuid(), playerModel -> {
            playerModel.gamesPlayed++;
            playerModel.save();
        }));

        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();
    }

    public static void call() {
        Groups.player.each(player -> PlayerModel.find(player.uuid(), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));
    }
}
