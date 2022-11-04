package darkdustry.features;

import arc.struct.IntMap;
import arc.util.Time;
import mindustry.game.EventType.TapEvent;
import mindustry.world.Tile;

import static arc.util.Time.timeSinceMillis;
import static darkdustry.PluginVars.doubleTapDuration;

public class DoubleTap {

    public static final IntMap<Tap> lastTaps = new IntMap<>();

    public static void clear() {
        lastTaps.clear();
    }

    public static void check(TapEvent event, Runnable runnable) {
        if (lastTaps.containsKey(event.player.id) && lastTaps.remove(event.player.id).check(event))
            runnable.run();
        else lastTaps.put(event.player.id, new Tap(event.tile, Time.millis()));
    }

    public record Tap(Tile tile, long time) {
        public boolean check(TapEvent event) {
            return event.tile == tile && timeSinceMillis(time) < doubleTapDuration;
        }
    }
}