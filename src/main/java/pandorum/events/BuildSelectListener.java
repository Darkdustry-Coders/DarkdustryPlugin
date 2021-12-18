package pandorum.events;

import com.mongodb.BasicDBObject;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import pandorum.PandorumPlugin;
import pandorum.annotations.events.EventListener;
import pandorum.models.PlayerModel;

import static pandorum.Misc.bundled;

public class BuildSelectListener {
    @EventListener(eventType = EventType.BuildSelectEvent.class)
            public static void call(final EventType.BuildSelectEvent event) {
        if (PandorumPlugin.config.mode.isSimple && !event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < 150) && PandorumPlugin.interval.get(0, 900f)) {
            Groups.player.each(p -> p.team() == event.team, p -> PlayerModel.find(new BasicDBObject("UUID", p.uuid()), playerInfo -> {
                if (playerInfo.alerts) {
                    bundled(p, "events.alert",
                            event.builder.getPlayer().coloredName(),
                            event.tile.x,
                            event.tile.y
                    );
                }
            }));
        }
    }
}
