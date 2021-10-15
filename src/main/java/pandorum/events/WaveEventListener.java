package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import org.bson.Document;
import pandorum.PandorumPlugin;

import static mindustry.Vars.state;

public class WaveEventListener {
    public static void call(final EventType.WaveEvent event) {
        Groups.player.each(p -> p.team() != state.rules.waveTeam, p -> {
            Document playerInfo = PandorumPlugin.createInfo(p);
            int wavesSurvived = playerInfo.getInteger("waves") + 1;
            playerInfo.replace("waves", wavesSurvived);
            PandorumPlugin.savePlayerStats(p.uuid());
        });
    }
}
