package pandorum.events.listeners;

import mindustry.game.EventType.BuildSelectEvent;
import mindustry.gen.Groups;
import mindustry.world.blocks.power.NuclearReactor;
import pandorum.comp.Icons;
import pandorum.models.PlayerModel;

import static pandorum.util.Utils.bundled;
import static pandorum.PluginVars.*;

public class BuildSelectListener {

    public static void call(final BuildSelectEvent event) {
        if (config.alertsEnabled() && !event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block instanceof NuclearReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c) < alertsDistance) && interval.get(0, 600f)) {
            Groups.player.each(player -> player.team() == event.team, player -> PlayerModel.find(player, playerModel -> {
                if (playerModel.alerts) bundled(player, "events.alert", event.builder.getPlayer().coloredName(), Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
            }));
        }
    }
}
