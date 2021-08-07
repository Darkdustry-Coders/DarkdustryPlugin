package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.content.Blocks;
import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import static pandorum.Misc.bundled;

public class BuildSelectEvent {
    public static void call(final EventType.BuildSelectEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        if(!event.breaking && event.builder != null && event.builder.buildPlan() != null &&
            event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() &&
            event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < config.alertDistance)){
            Player target = event.builder.getPlayer();

            if(interval.get(300)){
                Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.alert", target.name, event.tile.x, event.tile.y));
            }
        }
    }
}
