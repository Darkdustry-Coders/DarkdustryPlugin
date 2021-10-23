package pandorum.models;

import java.util.function.Consumer;

import org.bson.conversions.Bson;

import pandorum.database.MongoDataBridge;
import pandorum.ranks.Ranks;

public class PlayerModel extends MongoDataBridge<PlayerModel> {
    public String UUID;
    public String locale;
    public boolean hellomsg = true;
    public boolean alerts = true;
    public long playTime = 0;
    public int buildingsBuilt = 0;
    public int buildingsDeconstructed = 0;
    public int maxWave = 0;
    public int gamesPlayed = 0;
    public Ranks.Rank rank = Ranks.player;

    public static void find(Bson filter, Consumer<PlayerModel> callback) {
        PlayerModel.findAndApplySchema(PlayerModel.class, filter, callback);
    }
}
