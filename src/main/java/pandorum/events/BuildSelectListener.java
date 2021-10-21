package pandorum.events;

import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.comp.Config;

import static pandorum.Misc.bundled;

public class BuildSelectListener {
    public static void call(final EventType.BuildSelectEvent event) {
        if (PandorumPlugin.config.mode == Config.Gamemode.hexed || PandorumPlugin.config.mode == Config.Gamemode.hub || PandorumPlugin.config.mode == Config.Gamemode.castle) return;

        if (!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() && event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)) {
            Player builder = event.builder.getPlayer();

            if (PandorumPlugin.interval.get(0, 750f)) {
                Groups.player.each(p -> {
                    Document playerInfo = PandorumPlugin.createInfo(p);
                    if (playerInfo.getBoolean("alerts")) bundled(p, "events.alert", builder.coloredName(), event.tile.x, event.tile.y);
                });
            }
        }
    }
}
