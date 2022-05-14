package pandorum.mongo.models;

import arc.func.Cons;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.mongo.MongoDataBridge;

public class PlayerModel extends MongoDataBridge<PlayerModel> {

    private static MongoCollection<Document> collection;

    public String UUID;

    public String locale = "off";
    public boolean welcomeMessage = true;
    public boolean alerts = true;
    public int playTime = 0;
    public int buildingsBuilt = 0;
    public int buildingsDeconstructed = 0;
    public int gamesPlayed = 0;
    public int rank = 0;

    public static void setCollection(MongoCollection<Document> collection) {
        PlayerModel.collection = collection;
    }

    public static void find(Player player, Cons<PlayerModel> cons) {
        if (player != null && !player.isLocal()) find(player.uuid(), cons);
    }

    public static void find(String UUID, Cons<PlayerModel> cons) {
        findAndApplySchema(collection, PlayerModel.class, new BasicDBObject("UUID", UUID), cons);
    }

    public void save() {
        save(collection);
    }
}