package pandorum;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.ClientCommandsLoader;
import pandorum.commands.ServerCommandsLoader;
import pandorum.components.Config;
import pandorum.database.models.MapModel;
import pandorum.database.models.PlayerModel;
import pandorum.events.Loader;

import static mindustry.Vars.dataDirectory;
import static pandorum.PluginVars.*;

public final class PandorumPlugin extends Plugin {
    public PandorumPlugin() {
        Log.info("[Darkdustry] Плагин загружается...");

        Fi configFile = dataDirectory.child(configFileName);
        if (configFile.exists()) {
            config = gson.fromJson(configFile.reader(), Config.class);
            Log.info("[Darkdustry] Конфигурация загружена. (@)", configFile.absolutePath());
        } else {
            configFile.writeString(gson.toJson(config = new Config()));
            Log.info("[Darkdustry] Файл конфигурации сгенерирован. (@)", configFile.absolutePath());
        }

        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionStringUrl)).retryWrites(true).build();
        MongoClient client = MongoClients.create(settings);
        MongoDatabase database = client.getDatabase(databaseName);

        MongoCollection<Document> playersInfoCollection = database.getCollection(playersCollectionName);
        PlayerModel.setCollection(playersInfoCollection);

        MongoCollection<Document> mapsInfoCollection = database.getCollection(mapsCollectionName);
        MapModel.setCollection(mapsInfoCollection);

        Log.info("[Darkdustry] Плагин загружен...");
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