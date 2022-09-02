package darkdustry.components;

import arc.util.Log;
import com.mongodb.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.components.Database.PlayerData;
import darkdustry.utils.Utils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class MongoDatabase {

    public static MongoCollection<PlayerData> collection;

    public static void load() {
        try {
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            MongoClient client = MongoClients.create(config.mongoUrl);
            com.mongodb.client.MongoDatabase db = client.getDatabase("darkdustry").withCodecRegistry(pojoCodecRegistry);
            collection = db.getCollection("playerData", PlayerData.class);
            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
        PlayerData data = getPlayerData("ejdndhjdbhdbhcbdcbd");
        data.language = "ru_RU";
        data.playTime = 100000000;
        data.alertsEnabled = false;
        setPlayerData(data);
    }

    public static PlayerData getPlayerData(String uuid) {
        var data = collection.find(eq("uuid", uuid)).first();
        return Utils.notNullElse(data, new PlayerData(uuid));
    }

    public static void setPlayerData(PlayerData data) {
        if (!hasPlayerData(data.uuid)) {
            collection.insertOne(data);
        } else {
            collection.replaceOne(eq("uuid", data.uuid), data);
        }
    }

    public static boolean hasPlayerData(String uuid) {
        var data = collection.find(eq("uuid", uuid));
        return data != null;
    }
}
