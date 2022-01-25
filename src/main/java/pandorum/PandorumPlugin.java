package pandorum;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.commands.ClientCommandsLoader;
import pandorum.commands.ServerCommandsLoader;
import pandorum.comp.Config;
import pandorum.comp.TilesHistory;
import pandorum.entry.CacheEntry;
import pandorum.events.Loader;
import pandorum.models.PlayerModel;

import static mindustry.Vars.dataDirectory;
import static pandorum.PluginVars.*;

public final class PandorumPlugin extends Plugin {
    public static void main(String[] args) throws InterruptedException {
        TilesHistory<CacheEntry> history = new TilesHistory<>((byte) 8, 30, 100_000);

        for (int i = 0; i < 100_000; i++) {
            history.put((short) 10, (short) 10, new CacheEntry() {
                public final String name = "AUF AUFOVICH";
                public final short blockID = 6353;
                public final Object value = new Object() {public String meow = "MIOW MOTHER FUCKER";};
                public final boolean connect = true;
            });
        }

        history.getAll((short) 10, (short) 10, action -> {
            System.out.println(action.values());
        });

        Thread.sleep(1000 * 60);
    }

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
