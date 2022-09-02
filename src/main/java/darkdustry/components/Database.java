package darkdustry.components;

import darkdustry.DarkdustryPlugin;
import mindustry.gen.Player;
import org.bson.codecs.pojo.annotations.BsonCreator;
import redis.clients.jedis.*;

import static darkdustry.PluginVars.*;

public class Database {

    public static JedisPool jedisPool;

    public static void connect() {
        try {
            jedisPool = new JedisPool(new JedisPoolConfig(), config.jedisIp, config.jedisPort);
            jedisPool.getResource();
            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static PlayerData getPlayerData(String uuid) {
        try (var jedis = jedisPool.getResource()) {
            if (jedis.exists(uuid)) return gson.fromJson(jedis.get(uuid), PlayerData.class);
        } catch (Exception ignored) {}

        return new PlayerData(uuid);
    }

    public static void setPlayerData(PlayerData data) {
        try (var jedis = jedisPool.getResource()) {
            jedis.set(data.uuid, gson.toJson(data));
        } catch (Exception ignored) {}
    }

    public static boolean hasPlayerData(String uuid) {
        try (var jedis = jedisPool.getResource()) {
            return jedis.exists(uuid);
        } catch (Exception ignored) {
            return false;
        }
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

        public PlayerData() {

        }
        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}
