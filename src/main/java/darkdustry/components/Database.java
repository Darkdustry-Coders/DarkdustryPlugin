package darkdustry.components;

import arc.struct.Seq;
import arc.util.Time;
import com.mongodb.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.menus.MenuHandler.*;
import lombok.*;
import mindustry.gen.Player;
import useful.MongoRepository;

import java.util.Date;

import static darkdustry.PluginVars.*;

public class Database {

    public static MongoClient client;
    public static MongoDatabase database;

    public static MongoRepository<PlayerData> players;
    public static MongoRepository<Ban> bans;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            database = client.getDatabase("darkdustry");

            players = new MongoRepository<>(database, "players", PlayerData.class);
            players.ascendingIndex("uuid");
            players.watchAfterChange(Cache::update);

            bans = new MongoRepository<>(database, "bans", Ban.class);
            bans.ascendingIndex("unbanDate", 0L);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static void exit() {
        client.close();
    }

    // region player data

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static PlayerData getPlayerData(String uuid) {
        return players.get("uuid", uuid, new PlayerData(uuid));
    }

    public static void savePlayerData(PlayerData data) {
        players.replace("uuid", data.uuid, data);
    }

    @NoArgsConstructor
    public static class PlayerData {
        public String uuid;
        public String name = "<unknown>";

        public boolean alerts = true;
        public boolean history = false;
        public boolean welcomeMessage = true;
        public boolean discordLink = true;

        public Language language = Language.off;
        public EffectsPack effects = EffectsPack.none;

        public int playTime = 0;
        public int blocksPlaced = 0;
        public int blocksBroken = 0;
        public int wavesSurvived = 0;

        public int gamesPlayed = 0;
        public int attackWins = 0;
        public int pvpWins = 0;
        public int hexedWins = 0;

        public Rank rank = Rank.player;

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }

    // endregion
    // region ban

    public static Ban getBan(String uuid, String ip) {
        return bans.getOr("uuid", uuid, "ip", ip);
    }

    public static Seq<Ban> getBans() {
        return bans.all();
    }

    public static void addBan(Ban ban) {
        bans.insert(ban);
    }

    public static Ban removeBan(String value) {
        return bans.deleteOr("uuid", value, "ip", value, "player", value);
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ban {
        public String uuid, ip;
        public String player, admin;

        public String reason;
        public Date unbanDate;

        public boolean expired() {
            return unbanDate.getTime() < Time.millis();
        }

        public long remaining() {
            return unbanDate.getTime() - Time.millis();
        }
    }

    // endregion
}