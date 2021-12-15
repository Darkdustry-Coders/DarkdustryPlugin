package pandorum;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timekeeper;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import arc.util.serialization.Jval;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.game.Team;
import mindustry.io.JsonIO;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.ClientCommandsLoader;
import pandorum.commands.ServerCommandsLoader;
import pandorum.comp.AntiVPN;
import pandorum.comp.Config;
import pandorum.comp.Loader;
import pandorum.comp.Translator;
import pandorum.entry.HistoryEntry;
import pandorum.models.PlayerModel;
import pandorum.struct.CacheSeq;
import pandorum.vote.VoteKickSession;
import pandorum.vote.VoteSession;

import java.io.IOException;

import static mindustry.Vars.dataDirectory;

public final class PandorumPlugin extends Plugin {

    public static final VoteSession[] current = {null};
    public static final VoteKickSession[] currentlyKicking = {null};

    public static final ObjectMap<String, Timekeeper> nominateCooldowns = new ObjectMap<>(), votekickCooldowns = new ObjectMap<>(), loginCooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Team> spectating = new ObjectMap<>();
    public static final ObjectMap<Team, Seq<String>> votesSurrender = new ObjectMap<>();

    public static final Seq<String> votesRTV = new Seq<>(), votesVNW = new Seq<>(), activeHistoryPlayers = new Seq<>();

    public static final Interval interval = new Interval(2);

    public static Config config;
    public static CacheSeq<HistoryEntry>[][] history;

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;

    public static Translator translator;
    public static AntiVPN antiVPN;

    public static String discordServerLink = "https://dsc.gg/darkdustry";

    public PandorumPlugin() throws IOException {
        JsonIO.json.setUsePrototypes(false);
        Fi configFi = dataDirectory.child("config.json");
        if (configFi.exists()) {
            config = JsonIO.json.fromJson(Config.class, configFi);
            Log.info("[Darkdustry] Конфигурация загружена...");
        } else {
            configFi.writeString(Jval.read(JsonIO.json.toJson(config = new Config(), Config.class)).toString(Jval.Jformat.formatted));
            Log.info("[Darkdustry] Файл конфигурации сгенерирован... (@)", configFi.absolutePath());
        }
        JsonIO.json.setUsePrototypes(true);

        ConnectionString connString = new ConnectionString("mongodb://darkdustry:XCore2000@127.0.0.1:27017/?authSource=darkdustry");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();

        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("darkdustry");
        playersInfoCollection = database.getCollection("players");

        PlayerModel.setSourceCollection(playersInfoCollection);

        antiVPN = new AntiVPN(config.antiVPNAPIToken);
        translator = new Translator();
    }

    @Override
    public void init() {
        Loader.init();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommandsLoader.registerServerCommands(handler);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommandsLoader.registerClientCommands(handler);
    }
}
