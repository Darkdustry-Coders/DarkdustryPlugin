package pandorum.comp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import pandorum.entry.CacheEntry;

public class TilesHistory <T extends CacheEntry> {
    public AsyncCache<TileKey, T> historyCache;
    private byte maxTileHistoryCapacity;

    /**
     * @param maxTileHistoryCapacity Максимальный размер истории на 1 тайл
     */
    public TilesHistory(byte maxTileHistoryCapacity, int expireDelay, int historySize) {
        this.maxTileHistoryCapacity = maxTileHistoryCapacity;
        this.historyCache = Caffeine.newBuilder()
            .expireAfterWrite(expireDelay, TimeUnit.MINUTES)
            .maximumSize(historySize)
            .buildAsync();
    }

    public void getAll(short x, short y, Consumer<Map<TileKey, T>> action) {
        Map<TileKey, T> tileEntries = historyCache
            .asMap()
            .entrySet()
            .stream()
            .filter(entry -> {
                TileKey key = entry.getKey();

                return key.serialNumber > 0
                    && key.serialNumber < maxTileHistoryCapacity
                    && key.x == x
                    && key.y == y;
            }).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue().join()));
        action.accept(tileEntries);
    }

    public void put(short x, short y, T cacheEntry) {
        getAll(x, y, data -> {
            var entrySet = data.entrySet();

            byte serialNumber = entrySet.size() > 0 ? Collections.max(
                entrySet,
                (entry1, entry2) -> entry1.getKey().serialNumber - entry2.getKey().serialNumber
            ).getKey().serialNumber : 0;

            historyCache.put(
                new TileKey(x, y, (byte) (serialNumber + 1)),
                CompletableFuture.completedFuture(cacheEntry)
            );
        });
    }
}