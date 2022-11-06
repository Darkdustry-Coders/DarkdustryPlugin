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
        tile.getLinkedTiles(other -> get(other.array()).add(entry));
    }

    public static HistoryStack get(int index) {
        if (index >= history.length) return new HistoryStack();

        var entries = history[index];
        if (entries == null) history[index] = entries = new HistoryStack();
        return entries;
    }

    public static class HistoryStack extends Queue<HistoryEntry> {
        public HistoryStack() {
            super(maxHistoryCapacity);
        }

        @Override
        public void add(HistoryEntry entry) {
            super.add(entry);
            if (size > maxHistoryCapacity) removeFirst();
        }
    }
}