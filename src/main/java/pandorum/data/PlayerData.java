package pandorum.data;

import pandorum.features.Ranks;
import pandorum.features.Ranks.Rank;

public class PlayerData {
    public String locale = "off";
    public boolean welcomeMessage = true;
    public boolean alertsEnabled = true;

    public int playTime = 0;
    public int buildingsBuilt = 0;
    public int gamesPlayed = 0;

    public Rank rank = Ranks.player;

    public long discordID = 0;
}
