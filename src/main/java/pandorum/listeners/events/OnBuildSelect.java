package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.components.Icons;
import pandorum.data.PlayerData;
import pandorum.features.Alerts;
import pandorum.util.PlayerUtils;

import static pandorum.PluginVars.*;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.PlayerUtils.bundled;
import static pandorum.util.StringUtils.getUnitName;

public class OnBuildSelect implements Cons<BuildSelectEvent> {

    // TODO вынести код внутри метода в Alerts, упростить
    public void get(BuildSelectEvent event) {
        if (!alertsEnabled() || event.breaking || event.builder == null || event.builder.buildPlan() == null) return;

        if (Alerts.isDangerousBuild(event.builder.buildPlan().block, event.team, event.tile) && interval.get(alertsInterval)) {
            event.team.data().players.each(player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alert.dangerous-building", getUnitName(event.builder), Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
                }
            });
        }
    }
}
