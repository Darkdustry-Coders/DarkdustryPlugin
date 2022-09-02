package darkdustry.components;

import arc.util.Log;
import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.gen.Player;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.mongodb.*;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.util.Arrays;
import org.bson.Document;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;


import static darkdustry.PluginVars.config;

public class MongoDB {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        mongoClient = MongoClients.create(new ConnectionString(config.mongoConnectionString));
        database = mongoClient.getDatabase("darkdustry");
        collection = database.getCollection("playerData", PlayerData.class);
    }
    public static PlayerData getPlayerData(Player player) {
        var data = collection.find(Filters.where("_id==="+player.uuid()));
        var neko = Mono.from(data.first());
        var pd = neko.block();
        if(pd == null){
            return new PlayerData(player.uuid());
        }
        // todo
        return pd;
    }
    public static void setPlayerData(PlayerData data) {
        Mono.from(collection.insertOne(data)).subscribe();
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
