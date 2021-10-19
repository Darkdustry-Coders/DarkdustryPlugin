package pandorum.ranks;

import mindustry.Vars;
import mindustry.gen.Player;
import org.bson.Document;

import pandorum.PandorumPlugin;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;

public class Ranker {

    //В минутах
    public static int activePlaytime = 360, veteranPlaytime = 1000;
    public static int activeWavesSurvived = 250, veteranWavesSurvived = 500;
    public static int activeGamesWon = 10, veteranGamesWon = 25;

    public static void updatePlayerRank(Player player) {
        Document playerInfo = PandorumPlugin.createInfo(player);
        if (RankType.getByPermission(playerInfo.getInteger("permission")) == RankType.player && TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")) >= activePlaytime && playerInfo.getInteger("wavesSurvived") >= activeWavesSurvived && playerInfo.getInteger("gamesWon") >= activeGamesWon) {
            playerInfo.replace("permissiom", RankType.active.permission);
            bundled(player, "events.active-promotion");
            PandorumPlugin.savePlayerStats(player.uuid());
        }

        if (RankType.getByPermission(playerInfo.getInteger("permission")) == RankType.active && TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")) >= veteranPlaytime && playerInfo.getInteger("wavesSurvived") >= veteranWavesSurvived && playerInfo.getInteger("gamesWon") >= veteranGamesWon) {
            playerInfo.replace("permissiom", RankType.veteran.permission);
            bundled(player, "events.veteran-promotion");
            PandorumPlugin.savePlayerStats(player.uuid());
        }
    }
}
