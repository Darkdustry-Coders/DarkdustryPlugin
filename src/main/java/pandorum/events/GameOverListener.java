package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.annotations.events.EventListener;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;

public class GameOverListener {
    @EventListener(eventType = EventType.GameOverEvent.class)
    public static void call(final EventType.GameOverEvent event) {
        Groups.player.each(p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            playerInfo.gamesPlayed++;

            if (PandorumPlugin.config.mode.isPvP) {
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
