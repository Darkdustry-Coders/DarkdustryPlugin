package pandorum.data;

import arc.util.Log;
import mindustry.io.JsonIO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static pandorum.PluginVars.*;

public class Database {

    public static void connect() {
        try {
            jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", jedisPoolPort);
            jedis = jedisPool.getResource();
            Log.info("[Darkdustry] База данных успешно подключена.");
        } catch (Exception e) {
            Log.err("[Darkdustry] Не удалось подключиться к базе данных", e);
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(uuid) ? JsonIO.json.fromJson(PlayerData.class, jedis.get(uuid)) : new PlayerData(uuid);
        } catch (Exception e) {
            return new PlayerData(uuid);
        }
    }

    public static void setPlayerData(String uuid, PlayerData data) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(uuid, JsonIO.json.toJson(data));
        } catch (Exception ignored) {}
    }
}
