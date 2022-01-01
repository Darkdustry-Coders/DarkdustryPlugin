package pandorum.events;

import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.models.PlayerModel;

public class GameOverListener {

    public static void call(final GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(p.uuid(), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        PandorumPlugin.votesSurrender.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();

        PandorumPlugin.spectating.clear();
    }
}
