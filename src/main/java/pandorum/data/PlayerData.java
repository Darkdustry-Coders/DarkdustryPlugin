package pandorum.data;

public class PlayerData {
    public String uuid;
    public String language = "off";

    public boolean welcomeMessage = true;
    public boolean alertsEnabled = true;

    public int playTime = 0;
    public int buildingsBuilt = 0;
    public int gamesPlayed = 0;

    public int rank = 0;

    public PlayerData(String uuid) {
        this.uuid = uuid;
    }
}
