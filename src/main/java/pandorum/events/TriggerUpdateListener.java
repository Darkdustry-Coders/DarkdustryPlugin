package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Ranks;
import pandorum.comp.Effects;
import pandorum.models.PlayerModel;

public class TriggerUpdateListener {
    public static void update() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
        if (PandorumPlugin.interval.get(1, 60f)) {
            Groups.player.each(player -> PlayerModel.find(new BasicDBObject("UUID", player.uuid()), playerInfo -> {
                playerInfo.playTime += 1000;
                playerInfo.save();
            }));
        }

        if (PandorumPlugin.interval.get(2, 60f)) {
            Groups.player.each(player -> Ranks.getRank(player, rank -> player.name(rank.tag + "[#" + player.color.toString().toUpperCase() + "]" + player.getInfo().lastName)));
        }
    }
}
