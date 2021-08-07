package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.content.Blocks;

import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import static pandorum.Misc.*;

public class BuildSelectEvent {
    public static void call(final EventType.BuildSelectEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        if(!event.breaking && event.builder != null && event.builder.buildPlan() != null &&
            event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() &&
            event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)){
            Player target = event.builder.getPlayer();

            if(PandorumPlugin.interval.get(300)){
                Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.alert", colorizedName(target), event.tile.x, event.tile.y));
            }
        }
    }
}
