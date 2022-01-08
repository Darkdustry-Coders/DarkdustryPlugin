package pandorum;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.ClientCommandsLoader;
import pandorum.commands.ServerCommandsLoader;
import pandorum.comp.AntiVPN;
import pandorum.comp.Config;
import pandorum.comp.Loader;
import pandorum.comp.Translator;
import pandorum.models.PlayerModel;

import static mindustry.Vars.dataDirectory;
import static pandorum.PluginVars.*;

public final class PandorumPlugin extends Plugin {

    public static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public static MongoClient mongoClient;
    public static MongoCollection<Document> playersInfoCollection;

    public static ReusableByteOutStream writeBuffer;
    public static Writes outputBuffer;

    public static Translator translator;
    public static AntiVPN antiVPN;

    public PandorumPlugin() {
        Log.info("[Darkdustry] Плагин запускается...");

        Fi configFile = dataDirectory.child(configFileName);
        if (configFile.exists()) {
            config = gson.fromJson(configFile.reader(), Config.class);
            Log.info("[Darkdustry] Конфигурация загружена. (@)", configFile.absolutePath());
        } else {
            configFile.writeString(gson.toJson(config = new Config()));
            Log.info("[Darkdustry] Файл конфигурации сгенерирован. (@)", configFile.absolutePath());
        }

        ConnectionString connectionString = new ConnectionString(connectionStringUrl);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).retryWrites(true).build();

        mongoClient = MongoClients.create(settings);
        playersInfoCollection = mongoClient.getDatabase(databaseName).getCollection(collectionName);

        PlayerModel.setSourceCollection(playersInfoCollection);

        antiVPN = new AntiVPN();
        translator = new Translator();

        Log.info("[Darkdustry] Плагин запущен...");
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
