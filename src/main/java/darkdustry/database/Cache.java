package darkdustry.database;

import darkdustry.database.models.Mute;
import darkdustry.database.models.PlayerData;
import discord4j.common.util.Snowflake;
import mindustry.gen.Player;
import useful.ExtendedMap;

public class Cache {

    public static final ExtendedMap<String, PlayerData> cache = new ExtendedMap<>();
    public static final ExtendedMap<Snowflake, PlayerData> discordCache = new ExtendedMap<>();
    public static final ExtendedMap<String, Mute> mutes = new ExtendedMap<>();

    public static void put(Player player, PlayerData data) {
        cache.put(player.uuid(), data);
    }

    public static void put(Snowflake flake, PlayerData data) {
        if (discordCache.size > 100) discordCache.remove(discordCache.keys().next());
        discordCache.put(flake, data);
    }

    public static PlayerData get(Player player) {
        return cache.get(player.uuid());
    }

    public static PlayerData get(String uuid) {
        return cache.get(uuid);
    }

    public static PlayerData get(int id) {
        return cache.findValue(data -> data.id == id);
    }

    public static PlayerData get(Snowflake flake) {
        return discordCache.get(flake);
    }

    public static PlayerData remove(Player player) {
        return cache.remove(player.uuid());
    }
}