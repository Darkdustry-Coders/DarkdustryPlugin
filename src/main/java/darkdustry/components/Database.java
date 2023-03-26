package darkdustry.components;

import arc.func.*;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.serialization.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.menus.MenuHandler.*;
import mindustry.gen.Player;
import mindustry.io.JsonIO;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;

import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static mindustry.Vars.dataDirectory;

public class Database {

    public static MongoClient client;
    public static MongoDatabase database;
    public static MongoCollection<PlayerData> playersCollection;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            database = client.getDatabase("darkdustry").withCodecRegistry(CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())));

            playersCollection = database.getCollection("players", PlayerData.class);

            class PlayerData implements Json.JsonSerializable {
                public String name;
                public int wins;

                @Override
                public void write(Json json) {
                    json.writeValue("name", name);
                    json.writeValue("wins", wins);
                }

                @Override
                public void read(Json json, JsonValue value) {
                    name = value.getString("name");
                    wins = value.getInt("wins");
                }
            }

            ObjectMap<String, PlayerData> map = JsonIO.json.fromJson(ObjectMap.class, PlayerData.class, dataDirectory.child("database.json"));

            Flux.fromIterable(new ObjectMap.Entries<>(map))
                    .filter(entry -> entry.value.wins > 0)
                    .flatMap(entry -> {
                        return getPlayerData(entry.key).zipWith(Mono.just(entry.value));
                    }).flatMap(tuple -> {
                        Log.info(tuple.getT1().name);
                        tuple.getT1().hexedWins = tuple.getT2().wins;
                        return setPlayerData(tuple.getT1());
                    }).subscribe();

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

    public static Flux<Tuple2<Player, PlayerData>> getPlayersData(Iterable<Player> players, Cons2<Player, PlayerData> cons) {
        return Flux.fromIterable(players)
                .flatMap(player -> Mono.zip(Mono.just(player), getPlayerData(player)))
                .doOnNext(tuple -> cons.get(tuple.getT1(), tuple.getT2()));
    }

    public static Mono<UpdateResult> setPlayerData(PlayerData data) {
        return Mono.from(playersCollection.replaceOne(eq("uuid", data.uuid), data, new ReplaceOptions().upsert(true)));
    }

    public static void updatePlayerData(Player player, Cons<PlayerData> cons) {
        updatePlayerData(player.uuid(), cons);
    }

    public static void updatePlayerData(String uuid, Cons<PlayerData> cons) {
        getPlayerData(uuid).flatMap(data -> {
            cons.get(data);
            return setPlayerData(data);
        }).subscribe();
    }

    public static void updatePlayersData(Iterable<Player> players, Cons2<Player, PlayerData> cons) {
        getPlayersData(players, cons).flatMap(tuple -> setPlayerData(tuple.getT2())).subscribe();
    }

    public static class PlayerData {
        public String uuid;
        public String name = "<unknown>";

        public boolean alerts = true;
        public boolean history = false;
        public boolean welcomeMessage = true;

        public Language language = Language.off;
        public EffectsPack effects = EffectsPack.none;

        public int playTime = 0;
        public int blocksPlaced = 0;
        public int blocksBroken = 0;
        public int wavesSurvived = 0;

        public int gamesPlayed = 0;
        public int attackWins = 0;
        public int pvpWins = 0;
        public int hexedWins = 0;

        public Rank rank = Rank.player;

        @SuppressWarnings("unused")
        public PlayerData() {
        }

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}