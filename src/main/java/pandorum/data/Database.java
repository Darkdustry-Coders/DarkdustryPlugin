package pandorum.data;

import arc.util.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static pandorum.PluginVars.*;

public class Database {

    public static void connect() {
        try {
            jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", jedisPoolPort);
            Log.info("[Darkdustry] База данных успешно подключена...");
        } catch (Exception e) {
            Log.err("[Darkdustry] Не удалось подключиться к базе данных", e);
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.exists(uuid)) return gson.fromJson(jedis.get(uuid), PlayerData.class);
        } catch (Exception ignored) {}

        return new PlayerData();
    }

    public static void setPlayerData(String uuid, PlayerData data) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(uuid, gson.toJson(data));
        } catch (Exception ignored) {}
    }
}
