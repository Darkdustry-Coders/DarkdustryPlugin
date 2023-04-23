package darkdustry.components;

import arc.struct.ObjectMap;
import darkdustry.components.Database.PlayerData;
import mindustry.gen.Player;

public class Cache {

    public static final ObjectMap<String, PlayerData> cache = new ObjectMap<>();

    public static PlayerData put(Player player, PlayerData data) {
        return cache.put(player.uuid(), data);
    }

    public static PlayerData get(Player player) {
        return cache.get(player.uuid());
    }

    public static PlayerData remove(Player player) {
        return cache.remove(player.uuid());
    }

    public static boolean contains(Player player) {
        return cache.containsKey(player.uuid());
    }

    public static void update(PlayerData data) {
        if (cache.containsKey(data.uuid))
            cache.put(data.uuid, data);
    }
}