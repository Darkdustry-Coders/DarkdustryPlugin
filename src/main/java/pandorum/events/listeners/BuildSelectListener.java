package pandorum.events.listeners;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.features.antigrief.Alerts;

public class BuildSelectListener implements Cons<BuildSelectEvent> {

    public void get(BuildSelectEvent event) {
        Alerts.buildAlert(event);
    }
}
