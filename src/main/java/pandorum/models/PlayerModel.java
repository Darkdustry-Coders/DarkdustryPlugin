package pandorum.models;

import org.darkdustry.MongoDataBridge;

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
}
