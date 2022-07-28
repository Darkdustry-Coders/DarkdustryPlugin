package pandorum.features;

import mindustry.world.Tile;
import pandorum.features.history.HistorySeq;
import pandorum.features.history.entry.HistoryEntry;

import static mindustry.Vars.world;
import static pandorum.PluginVars.*;

public class History {

    public static HistorySeq[][] history;

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }

    public static void clear() {
        history = new HistorySeq[world.width()][world.height()];
    }

    public static void putTileHistory(HistoryEntry entry, Tile tile) {
        tile.getLinkedTiles(t -> putHistory(entry, t.x, t.y));
    }

    public static void putHistory(HistoryEntry entry, int x, int y) {
        getHistory(x, y).add(entry);
    }

    public static HistorySeq getHistory(int x, int y) {
        HistorySeq entries = history[x][y];
        if (entries == null) {
            history[x][y] = entries = new HistorySeq(maxTileHistoryCapacity);
        }

        return entries;
    }
}
