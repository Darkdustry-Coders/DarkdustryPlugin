package pandorum.listeners.events;

import arc.func.Cons;
import arc.math.geom.Point2;
import mindustry.game.EventType.ConfigEvent;
import mindustry.world.blocks.power.PowerNode.PowerNodeBuild;
import pandorum.features.History;
import pandorum.features.history.entry.ConfigEntry;
import pandorum.features.history.entry.HistoryEntry;
import pandorum.util.Utils;

public class OnConfig implements Cons<ConfigEvent> {

    // TODO refactor
    public void get(ConfigEvent event) {
        if (History.enabled() && event.player != null) {
            boolean connect = false;

            if (event.tile instanceof PowerNodeBuild build) {
                var link = Point2.unpack((int) event.value).sub(build.tileX(), build.tileY());
                for (Point2 linked : build.config()) {
                    if (link != null && link.x == linked.x && link.y == linked.y) {
                        connect = true;
                        break;
                    }
                }
            }

            HistoryEntry entry = new ConfigEntry(event, connect);
            event.tile.tile.getLinkedTiles(tile -> Utils.getHistory(tile.x, tile.y).add(entry));
        }
    }
}
