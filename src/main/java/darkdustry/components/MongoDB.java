package darkdustry.components;

import arc.util.Log;
import com.mongodb.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Utils;
import mindustry.gen.Player;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class MongoDB {

    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        try {
            collection = MongoClients.create(config.mongoUrl)
                    .getDatabase("darkdustry").withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())))
                    .getCollection("playerData", PlayerData.class);

            DarkdustryPlugin.info("Database connected. (@ total values)", collection.countDocuments());
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        try {
            return Utils.notNullElse(collection.find(eq("uuid", uuid)).first(), new PlayerData(uuid));
        } catch (Exception e) {
            Log.err(e);
            return new PlayerData(uuid);
        }
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static void setPlayerData(PlayerData data) {
        try {
            collection.replaceOne(eq("uuid", data.uuid), data);
        } catch (Exception e) {
            Log.err(e);
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

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}
