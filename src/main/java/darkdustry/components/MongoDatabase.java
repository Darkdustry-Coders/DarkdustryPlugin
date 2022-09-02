package darkdustry.components;

import arc.util.Log;
import com.mongodb.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.components.Database.PlayerData;
import darkdustry.utils.Utils;
import org.bson.Document;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class MongoDatabase {

    public static MongoCollection<PlayerData> collection;

    public static void load() {
        try (var client = MongoClients.create(config.mongoUrl)) {
            collection = client.getDatabase("darkdustry")
                    .withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())))
                    .getCollection("players", PlayerData.class);
            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }

        PlayerData data = new PlayerData("hentai");
        data.rank = 1000 - 7;
        setPlayerData(data);

        PlayerData test = getPlayerData(data.uuid);
        Log.info("Test: @", test.rank);
    }

    public static PlayerData getPlayerData(String uuid) {
        var data = collection.find(new Document("uuid", uuid)).first();
        return Utils.notNullElse(data, new PlayerData(uuid));
    }

    public static void setPlayerData(PlayerData data) {
        if (!hasPlayerData(data.uuid)) {
            collection.insertOne(data);
            return;
        }

        collection.replaceOne(eq("uuid", data.uuid), data);
    }

    public static boolean hasPlayerData(String uuid) {
        var data = collection.find(eq("uuid", uuid)).first();
        return data != null;
    }
}
