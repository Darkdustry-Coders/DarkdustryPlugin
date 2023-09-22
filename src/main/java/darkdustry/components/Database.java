package darkdustry.components;

import arc.struct.Seq;
import arc.util.*;
import com.mongodb.client.*;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.menus.MenuHandler.*;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;
import useful.MongoRepository;

import java.util.Date;

import static arc.util.Strings.*;
import static com.mongodb.client.model.Filters.*;
import static darkdustry.components.Config.*;
import static darkdustry.utils.Utils.*;

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
            players.descendingIndex("id");
            players.descendingIndex("uuid");
            players.watchAfterChange(Cache::update);

            bans = new MongoRepository<>(database, "bans", Ban.class);
            bans.descendingIndex("id");
            bans.descendingIndex("unbanDate", 0L);

            Log.info("Database connected.");
        } catch (Exception e) {
            Log.err("Failed to connect to the database", e);
        }
    }

    // region player data

    public static PlayerData getPlayerData(String uuid) {
        return notNullElse(Cache.get(uuid), () -> players.get(eq("uuid", uuid)));
    }

    public static PlayerData getPlayerData(int id) {
        return notNullElse(Cache.get(id), () -> players.get(eq("pid", id)));
    }

    public static PlayerData getPlayerDataOrCreate(String uuid) {
        return players.get(eq("uuid", uuid), () -> {
            var data = new PlayerData(uuid);
            data.generateID();

            savePlayerData(data);
            return data;
        });
    }

    public static void savePlayerData(PlayerData data) {
        players.replace(eq("uuid", data.uuid), data);
    }

    // endregion
    // region ban

    public static Ban getBan(String uuid, String ip) {
        return bans.get(or(eq("uuid", uuid), eq("ip", ip)));
    }

    public static Seq<Ban> getBanned() {
        return bans.all();
    }

    public static void addBan(Ban ban) {
        bans.replace(or(eq("uuid", ban.uuid), eq("ip", ban.ip)), ban);
    }

    public static Ban removeBan(String input) {
        return bans.delete(or(eq("uuid", input), eq("ip", input), eq("pid", parseInt(input))));
    }

    @NoArgsConstructor
    public static class PlayerData {
        public String uuid;
        public String name = "<unknown>";

        @BsonProperty("pid")
        public int id;

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

        public void generateID() {
            this.id = players.generateNextID("pid");
        }

        public String plainName() {
            return stripColors(name);
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ban {
        public String uuid, ip;
        public String player, admin;

        @BsonProperty("pid")
        public int id;

        public String reason;
        public Date unbanDate;

        public void generateID() {
            this.id = players.getField(eq("uuid", uuid), "pid", -1);
        }

        public boolean expired() {
            return unbanDate.getTime() < Time.millis();
        }

        public long remaining() {
            return unbanDate.getTime() - Time.millis();
        }
    }

    // endregion
}