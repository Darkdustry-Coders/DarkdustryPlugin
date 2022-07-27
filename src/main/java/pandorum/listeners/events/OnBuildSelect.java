package pandorum.listeners.events;

import arc.func.Cons;
import mindustry.game.EventType.BuildSelectEvent;
import pandorum.components.Icons;
import pandorum.data.PlayerData;
import pandorum.features.Alerts;

import static pandorum.PluginVars.alertsInterval;
import static pandorum.PluginVars.interval;
import static pandorum.data.Database.getPlayerData;
import static pandorum.util.PlayerUtils.bundled;

public class OnBuildSelect implements Cons<BuildSelectEvent> {

    // TODO вынести код внутри метода в Alerts, упростить
    public void get(BuildSelectEvent event) {
        if (!Alerts.enabled() || event.breaking || event.builder == null || event.builder.buildPlan() == null || !event.builder.isPlayer()) return;

        if (Alerts.isDangerousBuild(event.builder.buildPlan().block, event.team, event.tile) && interval.get(alertsInterval * 60f)) {
            event.team.data().players.each(player -> {
                PlayerData data = getPlayerData(player.uuid());
                if (data.alertsEnabled) {
                    bundled(player, "alerts.dangerous-building", event.builder.getPlayer().name, Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
                }
            });
        }
    }
}
