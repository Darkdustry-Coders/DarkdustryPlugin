package pandorum.listeners.events;

import arc.func.Cons;
import arc.math.geom.Point2;
import mindustry.game.EventType.ConfigEvent;
import mindustry.world.blocks.power.PowerNode.PowerNodeBuild;
import pandorum.features.History;
import pandorum.features.history.entry.ConfigEntry;

public class OnConfig implements Cons<ConfigEvent> {

    public void get(ConfigEvent event) {
        if (History.enabled() && event.player != null) {
            boolean connect = false;

            if (event.tile instanceof PowerNodeBuild build) {
                var link = Point2.unpack((int) event.value).sub(build.tileX(), build.tileY());
                for (Point2 linked : build.config()) {
                    if (link.equals(linked)) {
                        connect = true;
                        break;
                    }
                }
            }

            var entry = new ConfigEntry(event, connect);
            History.putTileHistory(entry, event.tile.tile);
        }
    }
}
