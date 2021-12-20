package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.annotations.events.TriggerListener;
import pandorum.comp.Ranks;
import pandorum.comp.Effects;
import pandorum.models.PlayerModel;

public class TriggerUpdateListener {
    @TriggerListener(trigger = EventType.Trigger.update)
    public static void update() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
        if (PandorumPlugin.interval.get(1, 60f)) {
            Groups.player.each(player -> PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                playerInfo.playTime += 1000L;
                playerInfo.save();
            }));
        }

        if (PandorumPlugin.interval.get(2, 300f)) {
            Groups.player.each(Ranks::updateName);
        }
    }
}
