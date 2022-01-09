package pandorum.events.listeners;

import mindustry.gen.Groups;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.models.PlayerModel;

import static pandorum.PluginVars.interval;

public class TriggerUpdateListener {

    public static void update() {
        Groups.player.each(p -> p.unit().moving(), p -> Effects.onMove(p.x, p.y));

        if (interval.get(1, 60f)) {
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
