package pandorum.events.listeners;

import mindustry.gen.Groups;
import pandorum.comp.Effects;

public class TriggerUpdateListener {

    public static void update() {
        Groups.player.each(player -> player.unit().moving(), p -> Effects.onMove(p.x, p.y));
    }
}
