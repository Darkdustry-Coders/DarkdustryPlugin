package pandorum;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;

import mindustry.gen.Player;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.ClientCommandsLoader;
import pandorum.commands.ServerCommandsLoader;
import pandorum.comp.Config;
import pandorum.entry.CacheEntry;
import pandorum.events.Loader;
import pandorum.models.PlayerModel;

import static mindustry.Vars.dataDirectory;
import static pandorum.PluginVars.*;

public final class PandorumPlugin extends Plugin {
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

        MongoClient mongoClient = MongoClients.create(settings);
        MongoCollection<Document> playersInfoCollection = mongoClient.getDatabase(databaseName).getCollection(collectionName);

        PlayerModel.setSourceCollection(playersInfoCollection);

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

class Test implements CacheEntry {
    public final String name = "AUF AUFOVICH";
    public long blockID = 6353;
    public final Object value = new Object() {public String meow = "MIOW MOTHER FUCKER";};
    public final boolean connect = true;

    public Test(long a) {
        this.blockID = a;
    }

    @Override
    public String getMessage(Player player) {
        return "null";
    }
}