package darkdustry.database;

import arc.util.Log;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.ReturnDocument;
import darkdustry.database.models.*;
import dev.morphia.*;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import discord4j.common.util.Snowflake;
import mindustry.gen.Player;

import javax.annotation.Nullable;
import java.util.*;

import static darkdustry.config.Config.*;

public class Database {

    public static Datastore datastore;
    public static Mapper mapper;

    public static void connect() {
        try {
            datastore = Morphia.createDatastore(MongoClients.create(config.mongoUrl), "darkdustry");
            mapper = datastore.getMapper();

            mapper.getEntityModel(Mute.class);
            mapper.getEntityModel(Ban.class);
            mapper.getEntityModel(Counter.class);
            mapper.getEntityModel(PlayerData.class);
            mapper.getEntityModel(ServerConfig.class);

            datastore.ensureCaps();
            datastore.ensureIndexes();

            Log.info("Database connected.");
        } catch (Exception e) {
            Log.err("Failed to connect to the database", e);
        }
    }

    // region player data

    public static @Nullable PlayerData getPlayerData(Snowflake discordId) {
        var data = Cache.get(discordId);
        if (data != null) return data;
        data = datastore.find(PlayerData.class)
                .filter(Filters.eq("discordId", discordId.asLong()))
                .first();
        if (data == null) return null;
        var local = Cache.get(data.id);
        if (local != null) data = local;
        Cache.put(discordId, data);
        return data;
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

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

    public static @Nullable PlayerData getPlayerDataByCode(String code) {
        var data = datastore.find(PlayerData.class)
                .filter(Filters.eq("discordAttachCode", code))
                .first();
        if (data == null) return null;
        var local = Cache.get(data.id);
        if (local != null) return local;
        return data;
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
    // region mute

    public static Mute addMute(Mute mute) {
        Cache.mutes.put(mute.uuid, mute);
        return datastore.save(mute);
    }

    public static Mute removeMute(String uuid) {
        Cache.mutes.put(uuid, null);
        return datastore.find(Mute.class)
                .filter(Filters.or(Filters.eq("uuid", uuid)))
                .findAndDelete();
    }

    public static Mute getMute(String uuid) {
        if (Cache.mutes.containsKey(uuid)) {
            var cached = Cache.mutes.get(uuid);
            if (cached != null && cached.expired()) {
                Cache.mutes.put(uuid, null);
                return null;
            }
            return cached;
        }
        var db = datastore.find(Mute.class)
                .filter(Filters.or(Filters.eq("uuid", uuid)))
                .first();
        Cache.mutes.put(uuid, db);
        return db;
    }

    public static List<Mute> getMutes() {
        return datastore.find(Mute.class).stream().toList();
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
    // region config

    public static ServerConfig fetchConfig() {
        return fetchConfig("global");
    }
    public static ServerConfig fetchConfig(String namespace) {
        var config = datastore.find(ServerConfig.class)
                .filter(Filters.eq("namespace", namespace))
                .first();
        if (config == null) {
            config = new ServerConfig();
            config.namespace = namespace;
        }
        return config;
    }

    public static void writeConfig(ServerConfig config) {
        datastore.save(config);
    }

    // endregion
}
