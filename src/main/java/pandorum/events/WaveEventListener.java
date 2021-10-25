package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.models.PlayerModel;

import static mindustry.Vars.state;

public class WaveEventListener {
    public static void call(final EventType.WaveEvent event) {
        Groups.player.each(p -> p.team() != state.rules.waveTeam, p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
            if (playerInfo.maxWave < state.wave) {
                playerInfo.maxWave = state.wave;
                playerInfo.save();
            }
        }));
    }
}
