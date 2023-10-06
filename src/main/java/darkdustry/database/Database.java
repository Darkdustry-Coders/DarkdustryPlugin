package darkdustry.database;

import arc.util.Log;
import com.mongodb.client.MongoClients;
import darkdustry.database.models.*;
import dev.morphia.*;
import dev.morphia.mapping.*;
import dev.morphia.query.*;

import java.util.*;

import static darkdustry.config.Config.*;
import static dev.morphia.query.filters.Filters.*;

public class Database {

    public static Datastore datastore;
    public static Mapper mapper;

    public static void connect() {
        try {
            datastore = Morphia.createDatastore(MongoClients.create(config.mongoUrl), "darkdustry");
            mapper = datastore.getMapper();

            mapper.getEntityModel(PlayerData.class);
            mapper.getEntityModel(Ban.class);

            datastore.ensureCaps();
            datastore.ensureIndexes();

            Log.info("Database connected.");
        } catch (Exception e) {
            Log.err("Failed to connect to the database", e);
        }
    }

    // region player data

    public static PlayerData getPlayerData(String uuid) {
        return Optional.ofNullable(Cache.get(uuid)).orElseGet(() -> datastore.find(PlayerData.class)
                .filter(eq("uuid", uuid))
                .first());
    }

    public static PlayerData getPlayerData(int id) {
        return Optional.ofNullable(Cache.get(id)).orElseGet(() -> datastore.find(PlayerData.class)
                .filter(eq("_id", id))
                .first());
    }

    public static PlayerData getPlayerDataOrCreate(String uuid) {
        return Optional.ofNullable(datastore.find(PlayerData.class).filter(eq("uuid", uuid)).first()).orElseGet(() -> {
            var data = new PlayerData(uuid);
            data.generateID();

            return savePlayerData(data);
        });
    }

    public static PlayerData savePlayerData(PlayerData data) {
        return datastore.save(data);
    }

    // endregion
    // region ban

    public static void addBan(Ban ban) {
        datastore.save(ban);
    }

    public static Ban removeBan(String uuid, String ip) {
        return datastore.find(Ban.class)
                .filter(or(eq("uuid", uuid), eq("ip", ip)))
                .findAndDelete();
    }

    public static Ban getBan(String uuid, String ip) {
        return datastore.find(Ban.class)
                .filter(or(eq("uuid", uuid), eq("ip", ip)))
                .first();
    }

    public static List<Ban> getBans() {
        return datastore.find(Ban.class).stream().toList();
    }

    // endregion
    // region ID

    public static int generateNextID(Class<?> type) {
        try (var iterator = datastore.find(type).iterator(new FindOptions()
                .sort(Sort.descending("_id"))
                .limit(1)
        )) {
            return iterator.hasNext() ? mapper.getId(iterator.next()) instanceof Integer id ? id + 1 : 0 : 0;
        }
    }

    // endregion
}