package pandorum.events;

import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.effects.Effects;
import pandorum.ranks.Ranker;

public class TriggerUpdateListener {
    public static void call() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
        if (PandorumPlugin.interval.get(1, 6f)) Groups.player.each(Ranker::updatePlayerRank);
    }
}
