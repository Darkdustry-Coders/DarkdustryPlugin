package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.models.PlayerModel;

public class TriggerUpdateListener {

    public static void update() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);

        if (PandorumPlugin.interval.get(1, 60f)) {
            Groups.player.each(p -> {
                PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                    playerInfo.playTime += 1000L;
                    playerInfo.save();
                });

                Ranks.updateName(p);
            });
        }
    }
}
