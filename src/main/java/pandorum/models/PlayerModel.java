package pandorum.models;

import org.darkdustry.MongoDataBridge;

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
}


