package pandorum.events;

import mindustry.gen.Groups;
import pandorum.effects.Effects;

public class TriggerUpdateListener {
    public static void call() {
        Groups.player.each(p -> p.unit().moving(), Effects::onMove);
    }
}
