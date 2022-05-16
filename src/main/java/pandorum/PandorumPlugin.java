package pandorum;

import arc.Core;
import arc.util.CommandHandler;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import mindustry.mod.Plugin;
import org.bson.Document;
import pandorum.mongo.models.MapModel;

import static pandorum.PluginVars.*;

public class PandorumPlugin extends Plugin {

    public PandorumPlugin() {
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionStringUrl)).retryWrites(true).build();
        MongoClient client = MongoClients.create(settings);
        MongoDatabase database = client.getDatabase(databaseName);

        MongoCollection<Document> mapsInfoCollection = database.getCollection(mapsCollectionName);
        MapModel.setCollection(mapsInfoCollection);
    }

    @Override
    public void init() {
        Core.app.addListener(new Main());
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = handler;
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        serverCommands = handler;
    }
}