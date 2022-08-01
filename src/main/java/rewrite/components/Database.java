package rewrite.components;

import mindustry.io.JsonIO;
import redis.clients.jedis.*;
import rewrite.DarkdustryPlugin;

import static pandorum.PluginVars.*;

public class Database {

    public static void connect() {
        try {
            jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", jedisPoolPort);
            jedisPool.getResource().ping();
            DarkdustryPlugin.info( "База данных успешно подключена.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Не удалось подключиться к базе данных: @", e.getMessage());
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(uuid) ? JsonIO.json.fromJson(PlayerData.class, jedis.get(uuid)) : new PlayerData();
        }
    }

    public static void setPlayerData(String uuid, PlayerData data) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(uuid, JsonIO.json.toJson(data));
        }
    }

    public static class PlayerData {
        public String uuid;
        public String translatorLanguage = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;
    }
}
