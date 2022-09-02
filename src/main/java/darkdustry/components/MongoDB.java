package darkdustry.components;

import arc.util.Log;
import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.gen.Player;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import static darkdustry.PluginVars.config;

public class MongoDB {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> collection;

    public static void connect() {
        mongoClient = MongoClients.create(new ConnectionString(config.mongoConnectionString));
        database = mongoClient.getDatabase("darkdustry");
        collection = database.getCollection("playerData");
    }
    public static PlayerData getPlayerData(Player player) {
        // todo
        return null;
    }
    public static void setPlayerData(PlayerData data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        Document doc = Document.parse(json);
        // todo: отправлять документ в базу данных
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

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}