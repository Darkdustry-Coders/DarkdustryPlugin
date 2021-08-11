package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.content.Blocks;

import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import pandorum.comp.*;
import static pandorum.Misc.*;

import java.awt.Color;

public class BuildSelectEvent {
    public static void call(final EventType.BuildSelectEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        if(!event.breaking && event.builder != null && event.builder.buildPlan() != null &&
            event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer() &&
            event.team.cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)){
            Player target = event.builder.getPlayer();

            if(PandorumPlugin.interval.get(300)){
                Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.alert", colorizedName(target), event.tile.x, event.tile.y));
                DiscordSender.send(colorizedName(target), "ВНИМАНИЕ!!! Данный игрок начал строить ториевый реактор возле ядра!", "X:", Short.toString(event.tile.x), "Y:", Short.toString(event.tile.y), new Color(204, 82, 27));
            }
        }
    }
}
