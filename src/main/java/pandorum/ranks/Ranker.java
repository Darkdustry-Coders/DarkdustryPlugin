package pandorum.ranks;

import mindustry.gen.Player;
import org.bson.Document;

import pandorum.PandorumPlugin;

import java.util.concurrent.TimeUnit;

import static pandorum.Misc.bundled;

public class Ranker {

    //В минутах
    public static int activePlaytime = 360, veteranPlaytime = 1000,
            activeWavesSurvived = 100, veteranWavesSurvived = 250,
            activeGamesWon = 10, veteranGamesWon = 25,
            activeBuildingsBuilt = 2500, veteranBuildingsBuilt = 7500;

    public static void updatePlayerRank(Player player) {
        Document playerInfo = PandorumPlugin.createInfo(player);
        if (RankType.getByNumber(playerInfo.getInteger("permission")) == RankType.player && TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")) >= activePlaytime && playerInfo.getInteger("wavesSurvived") >= activeWavesSurvived && playerInfo.getInteger("gamesWon") >= activeGamesWon && playerInfo.getLong("buildingsBuilt") >= activeBuildingsBuilt) {
            playerInfo.replace("rank", RankType.active.number);
            bundled(player, "events.active-promotion");
            PandorumPlugin.savePlayerStats(player.uuid());
        }

        if (RankType.getByNumber(playerInfo.getInteger("permission")) == RankType.active && TimeUnit.MILLISECONDS.toMinutes(playerInfo.getLong("playtime")) >= veteranPlaytime && playerInfo.getInteger("wavesSurvived") >= veteranWavesSurvived && playerInfo.getInteger("gamesWon") >= veteranGamesWon && playerInfo.getLong("buildingsBuilt") >= veteranBuildingsBuilt) {
            playerInfo.replace("rank", RankType.veteran.number);
            bundled(player, "events.veteran-promotion");
            PandorumPlugin.savePlayerStats(player.uuid());
        }
    }
}
