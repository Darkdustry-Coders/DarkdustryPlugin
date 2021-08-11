package pandorum.events;

import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.content.Blocks;
import mindustry.content.Items;

import pandorum.PandorumPlugin;
import pandorum.comp.Config.PluginType;
import static pandorum.Misc.*;

public class DepositEvent {
    public static void call(final EventType.DepositEvent event) {
        if(PandorumPlugin.config.type == PluginType.other) return;
        if(event.tile.block() == Blocks.thoriumReactor && event.item == Items.thorium && event.player.team().cores().contains(c -> event.tile.dst(c.x, c.y) < PandorumPlugin.config.alertDistance)){
            Groups.player.each(p -> !PandorumPlugin.alertIgnores.contains(p.uuid()), p -> bundled(p, "events.withdraw-thorium", colorizedName(event.player), event.tile.tileX(), event.tile.tileY()));
            DiscordSender.send(colorizedName(target), "ВНИМАНИЕ!!! Данный игрок положил торий в реактор возле ядра!", "X:", Short.toString(event.tile.tileX()), "Y:", Short.toString(event.tile.tileY()), new Color(204, 82, 27));
        }
    }
}
