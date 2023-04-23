package darkdustry.components;

import arc.util.Threads;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.changestream.FullDocument;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks.Rank;
import darkdustry.features.menus.MenuHandler.*;
import mindustry.gen.Player;
import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.PojoCodecProvider;

import static arc.Core.*;
import static com.mongodb.client.model.Filters.*;
import static darkdustry.PluginVars.*;
import static darkdustry.utils.Utils.*;

public class Database {

    public static final CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public static MongoClient client;
    public static MongoDatabase database;
    public static MongoCollection<PlayerData> playersCollection;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            database = client.getDatabase("darkdustry").withCodecRegistry(registry);
            playersCollection = database.getCollection("players", PlayerData.class);

            Threads.daemon(() -> playersCollection.watch(PlayerData.class)
                    .fullDocument(FullDocument.UPDATE_LOOKUP)
                    .forEach(stream -> {
                        var data = stream.getFullDocument();
                        if (data == null) return;

                        app.post(() -> Cache.update(data));
                    }));

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static void exit() {
        client.close();
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static PlayerData getPlayerData(String uuid) {
        return notNullElse(playersCollection.find(eq("uuid", uuid)).first(), new PlayerData(uuid));
    }

    public static void savePlayerData(PlayerData data) {
        playersCollection.replaceOne(eq("uuid", data.uuid), data, new ReplaceOptions().upsert(true));
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
        public PlayerData() {}

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }
    }
}