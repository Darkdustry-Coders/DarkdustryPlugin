package darkdustry.database;

import arc.util.Log;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.ReturnDocument;
import darkdustry.database.models.*;
import dev.morphia.*;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;

import java.util.*;

import static darkdustry.config.Config.*;


public class Database {

    public static Datastore datastore;
    public static Mapper mapper;

    public static void connect() {
        try {
            datastore = Morphia.createDatastore(MongoClients.create(config.mongoUrl), "darkdustry");
            mapper = datastore.getMapper();

            mapper.getEntityModel(Ban.class);
            mapper.getEntityModel(Counter.class);
            mapper.getEntityModel(PlayerData.class);

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
                .filter(Filters.eq("uuid", uuid))
                .first());
    }

    public static PlayerData getPlayerData(int id) {
        return Optional.ofNullable(Cache.get(id)).orElseGet(() -> datastore.find(PlayerData.class)
                .filter(Filters.eq("_id", id))
                .first());
    }

    public static PlayerData getPlayerDataOrCreate(String uuid) {
        return Optional.ofNullable(datastore.find(PlayerData.class).filter(Filters.eq("uuid", uuid)).first()).orElseGet(() -> {
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

    public static Ban addBan(Ban ban) {
        return datastore.save(ban);
    }

    public static Ban removeBan(String uuid, String ip) {
        return datastore.find(Ban.class)
                .filter(Filters.or(Filters.eq("uuid", uuid), Filters.eq("ip", ip)))
                .findAndDelete();
    }

    public static Ban getBan(String uuid, String ip) {
        return datastore.find(Ban.class)
                .filter(Filters.or(Filters.eq("uuid", uuid), Filters.eq("ip", ip)))
                .first();
    }

    public static List<Ban> getBans() {
        return datastore.find(Ban.class).stream().toList();
    }

    // endregion
    // region ID

    public static int generateNextID(String key) {
        return Optional.ofNullable(datastore.find(Counter.class)
                .filter(Filters.eq("_id", key))
                .modify(new ModifyOptions().returnDocument(ReturnDocument.AFTER), UpdateOperators.inc("value"))
        ).orElseGet(() -> datastore.save(new Counter(key))).value;
    }

    // endregion
}