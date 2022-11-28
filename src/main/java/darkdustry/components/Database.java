package darkdustry.components;

import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks.Rank;
import mindustry.gen.Player;
import reactor.core.publisher.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class Database {

    public static MongoClient client;
    public static MongoDatabase database;
    public static MongoCollection<PlayerData> playersCollection;
    public static MongoCollection<VpnData> vpnCollection;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            database = client.getDatabase("darkdustry")
                    .withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())));

            playersCollection = database.getCollection("players", PlayerData.class);
            vpnCollection = database.getCollection("vpns", VpnData.class);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static void exit() {
        client.close();
    }

    public static Mono<PlayerData> getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static Mono<PlayerData> getPlayerData(String uuid) {
        return Mono.from(playersCollection.find(eq("uuid", uuid))).defaultIfEmpty(new PlayerData(uuid));
    }

    public static Flux<PlayerData> getPlayersData(Iterable<Player> players) {
        return Flux.fromIterable(players).flatMap(Database::getPlayerData);
    }

    public static Mono<UpdateResult> setPlayerData(PlayerData data) {
        return Mono.from(playersCollection.replaceOne(eq("uuid", data.uuid), data, new ReplaceOptions().upsert(true)));
    }

    public static Mono<VpnData> getVpnData(String ip) {
        return Mono.from(vpnCollection.find(eq("ip", ip))).switchIfEmpty(AntiVpn.checkIp(ip).flatMap(data -> Mono.from(vpnCollection.insertOne(data)).thenReturn(data)));
    }

    public static class PlayerData {
        public String uuid;
        public String language = "off";

        public String name = "<unknown>";

        public boolean alerts = true;
        public boolean effects = true;
        public boolean history = false;
        public boolean welcomeMessage = true;

        public int gamesPlayed = 0;
        public int wavesSurvived = 0;
        public int blocksPlaced = 0;
        public int blocksBroken = 0;
        public int playTime = 0;

        public Rank rank = Rank.player;

        @SuppressWarnings("unused")
        public PlayerData() {}

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class VpnData {
        public String ip;
        public boolean isVpn;

        @SuppressWarnings("unused")
        public VpnData() {}

        public VpnData(String ip, boolean isVpn) {
            this.ip = ip;
            this.isVpn = isVpn;
        }
    }
}