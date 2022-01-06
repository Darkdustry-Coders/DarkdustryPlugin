package pandorum.events;

import mindustry.game.EventType.BuildSelectEvent;
import mindustry.gen.Groups;
import mindustry.world.blocks.power.NuclearReactor;
import pandorum.comp.Icons;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.interval;

public class BuildSelectListener {

    public static void call(final BuildSelectEvent event) {
        if (config.alertsEnabled() && !event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block instanceof NuclearReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < config.alertsDistance) && interval.get(0, 600f)) {
            Groups.player.each(p -> p.team() == event.team, p -> PlayerModel.find(p.uuid(), playerInfo -> {
                if (playerInfo.alerts) bundled(p, "events.alert", event.builder.getPlayer().coloredName(), Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
            }));
        }
    }
}
