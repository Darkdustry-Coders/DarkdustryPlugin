package pandorum.database.models;

import arc.func.Cons;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import mindustry.gen.Player;
import org.bson.Document;
import pandorum.database.MongoDataBridge;

public class PlayerModel extends MongoDataBridge<PlayerModel> {

    public PlayerModel(MongoCollection<Document> collection) {
        super(collection);
    }

    public PlayerModel() {}

    public String UUID;

    public String locale = "off";
    public boolean welcomeMessage = true;
    public boolean alerts = true;
    public int playTime = 0;
    public int buildingsBuilt = 0;
    public int buildingsDeconstructed = 0;
    public int gamesPlayed = 0;
    public int rank = 0;

    public void find(Player player, Cons<PlayerModel> cons) {
        if (player != null) find(player.uuid(), cons);
    }

    public void find(String UUID, Cons<PlayerModel> cons) {
        findAndApplySchema(new BasicDBObject("UUID", UUID), cons);
    }
}