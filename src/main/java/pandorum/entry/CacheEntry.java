package pandorum.entry;

import mindustry.gen.Player;

public interface CacheEntry {

    /**
     * Сгенерировать сообщение из истории
     */
    public String getMessage(Player player);
}
