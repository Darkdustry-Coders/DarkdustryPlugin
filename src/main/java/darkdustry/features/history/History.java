package darkdustry.features.history;

import arc.struct.Queue;
import mindustry.world.Tile;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class History {

    public static HistoryQueue[] history;

    public static boolean enabled() {
        return config.mode.isDefault;
    }

    public static void reset() {
        history = new HistoryQueue[world.width() * world.height()];
    }

    public static void put(Tile tile, HistoryEntry entry) {
        if (tile == emptyTile) return;

        tile.getLinkedTiles(other -> {
            var queue = get(other.array());
            if (queue == null) return;

            queue.add(entry);
        });
    }

    public static HistoryQueue get(int index) {
        if (index < 0 || index >= history.length) return null;

        var queue = history[index];
        if (queue == null)
            history[index] = queue = new HistoryQueue(maxHistorySize);

        return queue;
    }

    public static class HistoryQueue extends Queue<HistoryEntry> {
        public HistoryQueue(int size) {
            super(size);
        }

        @Override
        public void add(HistoryEntry entry) {
            if (size == values.length)
                removeFirst();

            super.add(entry);
        }
    }
}