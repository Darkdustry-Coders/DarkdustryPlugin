package pandorum.events.listeners;

import mindustry.game.EventType.BuildSelectEvent;
import pandorum.components.Icons;
import pandorum.database.models.PlayerModel;
import pandorum.util.Utils;

import static pandorum.PluginVars.*;
import static pandorum.util.Utils.bundled;

public class BuildSelectListener {

    public static void call(final BuildSelectEvent event) {
        if (config.alertsEnabled() && !event.breaking && event.builder != null && event.builder.buildPlan() != null && dangerousBlocks.contains(event.builder.buildPlan().block) && event.team.cores().contains(c -> event.tile.dst(c) < alertsDistance) && interval.get(alertsTimer)) {
            String name = Utils.notNullElse(event.builder.getControllerName(), Icons.get(event.builder.type.name));

            Utils.eachPlayerInTeam(event.team, player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) {
                    bundled(player, "events.alert", name, Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
                }
            }));
        }
    }
}
