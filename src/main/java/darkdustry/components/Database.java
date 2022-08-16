package darkdustry.components;

import mindustry.gen.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import darkdustry.DarkdustryPlugin;

import static darkdustry.PluginVars.*;

public class Database {

    public static JedisPool jedisPool;

    public static void connect() {
        try {
            jedisPool = new JedisPool(new JedisPoolConfig(), config.jedisIp, config.jedisPort);
            jedisPool.getResource();
            DarkdustryPlugin.info("Database connected.");
        } catch (Exception exception) {
            DarkdustryPlugin.error("Failed to connect to the database: @", exception);
        }
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static PlayerData getPlayerData(String uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(uuid) ? gson.fromJson(jedis.get(uuid), PlayerData.class) : new PlayerData(uuid);
        } catch (Exception ignored) {
            return new PlayerData(uuid);
        }
    }

    public static void setPlayerData(PlayerData data) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(data.uuid, gson.toJson(data));
        } catch (Exception ignored) {}
    }

    public static class PlayerData {

        public String uuid;
        public String language = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}
