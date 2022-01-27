package pandorum.comp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import arc.math.Mathf;
import arc.struct.Seq;
import pandorum.entry.CacheEntry;

/**
 * Класс, реализующий хранение, получение и очисту кеша историй действий с тайлами
 */
public class TilesHistory <T extends CacheEntry> {
    public AsyncCache<TileKey, T> historyCache;
    private byte maxTileHistoryCapacity;

    /**
     * Конструктор класса {@link TilesHistory}
     * 
     * @param maxTileHistoryCapacity Максимальный размер истории на 1 тайл
     * @param expireDelay Время в минутах до удаления добавленного элемента
     * @param historySize Максимальный размер истории
     */
    public TilesHistory(byte maxTileHistoryCapacity, int expireDelay, int historySize) {
        this.maxTileHistoryCapacity = maxTileHistoryCapacity;
        this.historyCache = Caffeine.newBuilder()
            .expireAfterWrite(expireDelay, TimeUnit.MINUTES)
            .maximumSize(historySize)
            .buildAsync();
    }

    /**
     * Получить все записи о тайле по x и y, действует ограничение {@code maxTileHistoryCapacity}
     * 
     * @param x Позиция тайла по x
     * @param y Позиция тайла по y
     * @param valuesFunction Функция, аргументом которой передаётся сортированный список найденных значений
     * @param keysFunction Функция, аргументом которой передаётся сортированный список ключей
     * 
     * @example
     * 
     * <pre>
     * var history = new TilesHistory<CacheEntry>((byte) 8, 30, 100_000);
     * 
     * history.getAll((short) x, (short) y, history -> {
     *     System.out.println(history.size);
     * }, keys -> {
     *     System.out.println(keys.size);
     * });
     * </pre>
     */
    public void getAll(short x, short y, Consumer<Seq<T>> valuesFunction, Consumer<Seq<TileKey>> keysFunction) {
        Seq<T> values = new Seq<T>();
        values.setSize(maxTileHistoryCapacity);

        Seq<TileKey> keys = new Seq<TileKey>();
        keys.setSize(maxTileHistoryCapacity);

        historyCache
            .asMap()
            .entrySet()
            .stream()
            .filter(entry -> {
                TileKey key = entry.getKey();

                return key.serialNumber >= 0
                    && key.serialNumber < maxTileHistoryCapacity
                    && key.x == x
                    && key.y == y;
            }).collect(Collectors.toMap(
                keyMapper -> keyMapper.getKey(),
                valueMapper -> valueMapper.getValue().join()
            )).forEach(
                (key, value) -> {
                    int index = key.serialNumber;
                    
                    keys.set(index, key);
                    values.set(index, value);
                });
        if (valuesFunction != null) valuesFunction.accept(values);
        if (keysFunction != null) keysFunction.accept(keys);
    }

    /**
     * Получить все записи о тайле по x и y, действует ограничение {@code maxTileHistoryCapacity}
     * 
     * @param x Позиция тайла по x
     * @param y Позиция тайла по y
     * @param valuesFunction Функция, аргументом которой передаётся сортированный список найденных значений
     * 
     * @example
     * 
     * <pre>
     * var history = new TilesHistory<CacheEntry>((byte) 8, 30, 100_000);
     * 
     * history.getAll((short) x, (short) y, history -> {
     *     System.out.println(history.size);
     * });
     * </pre>
     */
    public void getAll(short x, short y, Consumer<Seq<T>> valuesFunction) {
        getAll(x, y, valuesFunction, null);
    }

    /**
     * Записать информацию о действии с тайлом по x и y, действует ограничение {@code maxTileHistoryCapacity}
     * 
     * @param x Позиция тайла по x
     * @param y Позиция тайла по y
     * @param cacheEntry Объект, который нужно кешировать
     * 
     * @example
     * 
     * <pre>
     * var history = new TilesHistory<CacheEntry>((byte) 8, 30, 100_000);
     * 
     * history.put((short) x, (short) y, new CacheEntry() {});
     * </pre>
     */
    public void put(short x, short y, T cacheEntry) {
        getAll(x, y, data -> {
            byte serialNumber = (byte) Mathf.clamp(
                data.count(value -> value != null),
                0,
                maxTileHistoryCapacity - 1
            );

            historyCache.put(
                new TileKey(x, y, serialNumber),
                CompletableFuture.completedFuture(cacheEntry)
            );
        }, keys -> {
            if (
                keys.count(key -> key != null) < maxTileHistoryCapacity
            ) return;
            
            historyCache.asMap().remove(keys.get(0));
            keys.forEach(key -> key.serialNumber--);
        });
    }
}