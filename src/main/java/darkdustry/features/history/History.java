package darkdustry.features.history;

import arc.struct.Queue;
import mindustry.world.Tile;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.*;

public class History {

    public static HistoryStack[] history;

    public static boolean enabled() {
        return config.mode.isDefault();
    }

    public static void clear() {
        history = new HistoryStack[world.width() * world.height()];
    }

    public static void put(HistoryEntry entry, Tile tile) {
        if (tile == emptyTile) return;

        tile.getLinkedTiles(other -> {
            var stack = get(other.array());
            if (stack == null) return;

            stack.add(entry);
        });
    }

    public static HistoryStack get(int index) {
        if (index < 0 || index >= history.length) return null;

        var stack = history[index];
        if (stack == null) history[index] = stack = new HistoryStack();
        return stack;
    }

    public static class HistoryStack extends Queue<HistoryEntry> {
        public HistoryStack() {
            super(maxHistoryCapacity);
        }

        @Override
        public void add(HistoryEntry entry) {
            if (size >= maxHistoryCapacity)
                removeFirst();

            super.add(entry);
        }
    }
}