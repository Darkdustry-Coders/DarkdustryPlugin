package pandorum.events;

import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.models.PlayerModel;

public class TriggerUpdateListener {

    public static void update() {
        Groups.player.each(p -> p.unit().moving(), p -> Effects.onMove(p.x, p.y));

        if (PandorumPlugin.interval.get(1, 60f)) {
            Groups.player.each(p -> {
                PlayerModel.find(p.uuid(), playerInfo -> {
                    playerInfo.playTime += 1000L;
                    playerInfo.save();
                });

                Ranks.updateName(p);
            });
        }
    }
}
