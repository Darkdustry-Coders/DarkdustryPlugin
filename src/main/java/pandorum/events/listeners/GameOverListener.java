package pandorum.events.listeners;

import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.models.PlayerModel;

import static pandorum.PluginVars.*;

public class GameOverListener {

    public static void call(final GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(p.uuid(), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        votesSurrender.clear();
        votesRTV.clear();
        votesVNW.clear();

        activeHistoryPlayers.clear();
        activeSpectatingPlayers.clear();
    }

    public static void call() {
        Groups.player.each(p -> PlayerModel.find(p.uuid(), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));
    }
}
