package pandorum.features;

import pandorum.features.history.HistorySeq;

import static mindustry.Vars.world;
import static pandorum.PluginVars.*;

public class History {

    public static HistorySeq[][] history;

    public static boolean enabled() {
        return defaultModes.contains(config.mode);
    }

    public static void reload() {
        history = new HistorySeq[world.width()][world.height()];
    }

    public static HistorySeq getHistory(int x, int y) {
        HistorySeq entries = history[x][y];
        if (entries == null) {
            history[x][y] = entries = new HistorySeq(maxTileHistoryCapacity);
        }

        return entries;
    }
}
