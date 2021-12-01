package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;

public class GameOverListener {
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            playerInfo.gamesPlayed++;

            if (state.rules.pvp) {
                if (p.team() == event.winner) playerInfo.pvpWinCount++;
                else playerInfo.pvpLoseCount++;
            }

            playerInfo.save();
        }));

        PandorumPlugin.surrendered.clear();
        PandorumPlugin.votesRTV.clear();
        PandorumPlugin.votesVNW.clear();
    }
}
