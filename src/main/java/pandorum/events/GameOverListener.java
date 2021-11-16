package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.models.PlayerModel;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            playerInfo.gamesPlayed++;
            playerInfo.save();
        }));

        PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
