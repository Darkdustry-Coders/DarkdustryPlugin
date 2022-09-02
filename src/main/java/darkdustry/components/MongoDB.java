package darkdustry.components;

import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Utils;
import org.bson.codecs.pojo.annotations.BsonProperty;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;

public class MongoDB {

    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        try (var client = MongoClients.create(config.mongoConnectionString)) {
            collection = client
                    .getDatabase("darkdustry")
                    .getCollection("playerData", PlayerData.class);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static PlayerData getPlayerData(String uuid) {
        return Utils.notNullElse(Mono.from(collection.find(eq(uuid)).first()).block(), new PlayerData(uuid));
    }

    public static void setPlayerData(PlayerData data) {
        Mono.from(collection.replaceOne(eq(data.uuid), data)).subscribe();
    }

    public static class PlayerData {
        @BsonProperty("_id")
        public String uuid;
        public String discord;
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
