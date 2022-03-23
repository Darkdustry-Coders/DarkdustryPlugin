package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.features.antigrief.Alerts;

public class OnBuildSelect implements Cons<BuildSelectEvent> {

    public void get(BuildSelectEvent event) {
        Alerts.buildAlert(event);
    }
}
