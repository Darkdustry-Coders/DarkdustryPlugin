package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.features.Alerts;

public class OnBuildSelect implements Cons<BuildSelectEvent> {

    public void get(BuildSelectEvent event) {
        if (!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.isPlayer()) {
            Alerts.buildAlert(event);
        }
    }
}
