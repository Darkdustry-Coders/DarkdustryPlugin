package darkdustry.database;

import arc.struct.Seq;
import arc.util.*;
import com.mongodb.client.*;
import darkdustry.database.models.*;
import useful.MongoRepository;

import static arc.util.Strings.*;
import static com.mongodb.client.model.Filters.*;
import static darkdustry.config.Config.*;
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

    // endregion
}