package darkdustry.components;

import arc.util.Log;
import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.utils.Utils;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.annotations.BsonProperty;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.reactivestreams.client.MongoClients.getDefaultCodecRegistry;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDB {

    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        try {
            MongoClient client = MongoClients.create(config.mongoUrl);
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            collection = client
                    .getDatabase("darkdustry").withCodecRegistry(pojoCodecRegistry)
                    .getCollection("playerData", PlayerData.class);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
        PlayerData data = getPlayerData("ddd");
        data.gamesPlayed = 1000;
        setPlayerData(data);
    }

    public static PlayerData getPlayerData(String uuid) {
        return Utils.notNullElse(Mono.from(collection.find(eq("uuid", uuid)).first()).block(), new PlayerData(uuid));
    }

    public static void setPlayerData(PlayerData data) {
        Mono.from(collection.replaceOne(eq("uuid", data.uuid), data)).block();
    }

    public static class PlayerData {
        public String uuid;
        public String discord;
        public String language = "off";

        public boolean welcomeMessage = true;
        public boolean alertsEnabled = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;
        public PlayerData() {}
        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}
