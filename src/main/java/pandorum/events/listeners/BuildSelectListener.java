package pandorum.events.listeners;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.antigrief.Alerts;
import pandorum.components.Icons;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;

public class BuildSelectListener implements Cons<BuildSelectEvent> {

    public void get(BuildSelectEvent event) {
        Alerts.buildAlert(event);
    }
}
