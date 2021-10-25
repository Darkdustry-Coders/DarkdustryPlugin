package pandorum.models;

import org.bson.conversions.Bson;

import java.util.function.Consumer;
import pandorum.database.MongoDataBridge;

public class PlayerModel extends MongoDataBridge<PlayerModel> {
    public String UUID;
    public String locale = "off";
    public boolean hellomsg = true;
    public boolean alerts = true;
    public long playTime = 0;
    public int buildingsBuilt = 0;
    public int buildingsDeconstructed = 0;
    public int maxWave = 0;
    public int gamesPlayed = 0;
    public int rank = 0;

    public static void find(Bson filter, Consumer<PlayerModel> callback) {
        PlayerModel.findAndApplySchema(PlayerModel.class, filter, callback);
    }
}


