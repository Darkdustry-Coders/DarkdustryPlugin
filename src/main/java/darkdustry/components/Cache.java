package darkdustry.components;

import arc.struct.IntMap;
import mindustry.gen.Player;
import darkdustry.components.Database.PlayerData;

public class Cache {

    public static final IntMap<PlayerData> cache = new IntMap<>();

    public static void put(Player player, PlayerData data) {
        cache.put(player.id, data);
    }

    public static PlayerData get(Player player) {
        return cache.get(player.id);
    }

    public static PlayerData remove(Player player) {
        return cache.remove(player.id);
    }

    public static void join(Player player) {
        var data = cache.get(player.id);
        if (data == null) return;

        data.effects.join.get(player);
    }

    public static void leave(Player player) {
        var data = cache.get(player.id);
        if (data == null) return;

        data.effects.leave.get(player);
    }

    public static void move(Player player) {
        if (!player.unit().moving()) return; // It's called here to prevent NPE

        var data = cache.get(player.id);
        if (data == null) return;

        data.effects.move.get(player);
    }
}