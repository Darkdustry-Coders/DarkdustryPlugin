package darkdustry.features.history;

import arc.math.Mathf;
import arc.struct.Queue;
import mindustry.world.Tile;

import static darkdustry.PluginVars.*;
import static mindustry.Vars.world;

public class History {

    public static HistoryStack[] history;

    public static boolean enabled() {
        return config.mode.isDefault();
    }

    public static void clear() {
        history = new HistoryStack[world.width() * world.height()];
    }

    public static void put(HistoryEntry entry, Tile tile) {
        tile.getLinkedTiles(other -> get(other.array()).add(entry));
    }

    public static HistoryStack get(int index) {
        index = Mathf.clamp(index, 0, history.length - 1);

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