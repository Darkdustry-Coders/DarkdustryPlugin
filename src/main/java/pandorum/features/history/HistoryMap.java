package pandorum.features.history;

import arc.Core;
import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.Seq;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import mindustry.world.Tile;
import pandorum.features.history.entry.HistoryEntry;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HistoryMap {

    public final int maxTileHistoryCapacity;
    public final AsyncCache<TileKey, HistoryEntry> historyCache;

    public HistoryMap(int maxTileHistoryCapacity, int allHistorySize) {
        this.maxTileHistoryCapacity = maxTileHistoryCapacity;
        this.historyCache = Caffeine.newBuilder().maximumSize(allHistorySize).buildAsync();
    }

    public void getAll(int x, int y, Cons<Seq<HistoryEntry>> valuesFunction, Cons<Seq<TileKey>> keysFunction) {
        Seq<HistoryEntry> values = new Seq<>();
        values.setSize(maxTileHistoryCapacity);

        Seq<TileKey> keys = new Seq<>();
        keys.setSize(maxTileHistoryCapacity);

        historyCache.asMap().entrySet().stream().filter(entry -> {
            TileKey key = entry.getKey();
            return key.serialNumber >= 0 && key.serialNumber < maxTileHistoryCapacity && key.x == x && key.y == y;
        }).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().join())).forEach((key, value) -> {
            int index = key.serialNumber;

            keys.set(index, key);
            values.set(index, value);
        });

        if (valuesFunction != null) valuesFunction.get(values.filter(Objects::nonNull));
        if (keysFunction != null) keysFunction.get(keys.filter(Objects::nonNull));
    }

    public void getAll(int x, int y, Cons<Seq<HistoryEntry>> valuesFunction) {
        getAll(x, y, valuesFunction, null);
    }

    public void put(int x, int y, HistoryEntry entry) {
        getAll(x, y, values -> {
            int serialNumber = Mathf.clamp(values.size, 0, maxTileHistoryCapacity - 1);
            historyCache.put(new TileKey(x, y, serialNumber), CompletableFuture.completedFuture(entry));
        }, keys -> {
            if (keys.size < maxTileHistoryCapacity) return;

            historyCache.asMap().remove(keys.first());
            keys.each(key -> key.serialNumber--);
        });
    }

    public void put(Tile tile, HistoryEntry entry) {
        Core.app.post(() -> put(tile.x, tile.y, entry));
    }

    public void putLinkedTiles(Tile tile, HistoryEntry entry) {
        tile.getLinkedTiles(t -> put(t, entry));
    }

    public void clear() {
        historyCache.asMap().clear();
    }
}